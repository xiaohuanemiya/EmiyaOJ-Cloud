package com.emiyaoj.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emiyaoj.auth.api.AuthUserFeignClient;
import com.emiyaoj.auth.vo.UserVO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.common.exception.BadRequestException;
import com.emiyaoj.common.exception.BaseException;
import com.emiyaoj.judge.api.SubmissionFeignClient;
import com.emiyaoj.judge.dto.SubmissionVO;
import com.emiyaoj.problem.domain.pojo.Contest;
import com.emiyaoj.problem.domain.pojo.ContestAdmin;
import com.emiyaoj.problem.domain.pojo.ContestProblem;
import com.emiyaoj.problem.domain.pojo.ContestRegistration;
import com.emiyaoj.problem.domain.pojo.Problem;
import com.emiyaoj.problem.dto.ContestAdminAssignDTO;
import com.emiyaoj.problem.dto.ContestProblemDTO;
import com.emiyaoj.problem.dto.ContestProblemVO;
import com.emiyaoj.problem.dto.ContestQueryDTO;
import com.emiyaoj.problem.dto.ContestRankProblemVO;
import com.emiyaoj.problem.dto.ContestRankUserVO;
import com.emiyaoj.problem.dto.ContestRankVO;
import com.emiyaoj.problem.dto.ContestRegisterDTO;
import com.emiyaoj.problem.dto.ContestRegistrationVO;
import com.emiyaoj.problem.dto.ContestSaveDTO;
import com.emiyaoj.problem.dto.ContestSubmitCheckVO;
import com.emiyaoj.problem.dto.ContestVO;
import com.emiyaoj.problem.mapper.ContestAdminMapper;
import com.emiyaoj.problem.mapper.ContestMapper;
import com.emiyaoj.problem.mapper.ContestProblemMapper;
import com.emiyaoj.problem.mapper.ContestRegistrationMapper;
import com.emiyaoj.problem.mapper.ProblemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContestService extends ServiceImpl<ContestMapper, Contest> {

    private static final String CONTEST_PERMISSION = "CONTEST";
    private static final String INVITE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*_-+=?";
    private static final int INVITE_LENGTH = 10;
    private static final int RULE_ACM = 1;
    private static final int RULE_IOI = 2;
    private static final int RULE_CODEFORCES = 3;
    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PUBLISHED = 1;
    private static final int STATUS_CANCELLED = 2;
    private static final int JUDGE_ACCEPTED = 2;
    private static final Set<Integer> WRONG_STATUSES = Set.of(5, 6, 7, 8, 9);
    private static final Comparator<LocalDateTime> NULLS_LAST_TIME =
            Comparator.nullsLast(LocalDateTime::compareTo);

    private final ContestProblemMapper contestProblemMapper;
    private final ContestRegistrationMapper contestRegistrationMapper;
    private final ContestAdminMapper contestAdminMapper;
    private final ProblemMapper problemMapper;
    private final ProblemService problemService;
    private final AuthUserFeignClient authUserFeignClient;
    private final SubmissionFeignClient submissionFeignClient;
    private final SecureRandom secureRandom = new SecureRandom();

    public PageVO<ContestVO> queryContestPage(ContestQueryDTO queryDTO, Long userId) {
        Page<Contest> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Contest> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getTitle()), Contest::getTitle, queryDTO.getTitle())
                .eq(queryDTO.getRuleType() != null, Contest::getRuleType, queryDTO.getRuleType())
                .eq(queryDTO.getStatus() != null, Contest::getStatus, queryDTO.getStatus())
                .ge(queryDTO.getStartFrom() != null, Contest::getStartTime, queryDTO.getStartFrom())
                .le(queryDTO.getStartTo() != null, Contest::getStartTime, queryDTO.getStartTo());
        if (queryDTO.getStatus() == null) {
            wrapper.and(userId != null,
                    w -> w.eq(Contest::getStatus, STATUS_PUBLISHED).or().eq(Contest::getCreatorId, userId));
            wrapper.eq(userId == null, Contest::getStatus, STATUS_PUBLISHED);
        }
        wrapper.orderByDesc(Contest::getStartTime).orderByDesc(Contest::getCreateTime);
        this.page(page, wrapper);
        return PageVO.of(page, contest -> toVO(contest, false, userId));
    }

    public ContestVO getContestDetail(Long id, Long userId) {
        Contest contest = this.getById(id);
        if (contest == null || !canView(contest, userId)) {
            return null;
        }
        return toVO(contest, true, userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public ContestVO saveContest(ContestSaveDTO dto, Long userId) {
        validateContestDTO(dto, false);
        Contest contest = new Contest();
        BeanUtils.copyProperties(dto, contest);
        contest.setFreezeBeforeMinutes(dto.getFreezeBeforeMinutes() == null ? 60 : dto.getFreezeBeforeMinutes());
        contest.setInviteCode(resolveInviteCode(dto.getInviteCode(), null));
        contest.setStatus(dto.getStatus() == null ? STATUS_DRAFT : dto.getStatus());
        contest.setCreatorId(userId);
        contest.setDeleted(0);
        contest.setCreateBy(userId);
        contest.setUpdateBy(userId);
        contest.setCreateTime(LocalDateTime.now());
        contest.setUpdateTime(LocalDateTime.now());
        this.save(contest);

        insertAdmin(contest.getId(), userId, userId);
        if (!CollectionUtils.isEmpty(dto.getProblems())) {
            replaceContestProblems(contest.getId(), dto.getProblems(), userId);
        }
        return toVO(contest, true, userId);
    }

    public boolean updateContest(ContestSaveDTO dto, Long userId) {
        validateContestDTO(dto, true);
        Contest contest = requireContestAdmin(dto.getId(), userId);
        contest.setTitle(dto.getTitle());
        contest.setDescription(dto.getDescription());
        contest.setRuleType(dto.getRuleType());
        contest.setStartTime(dto.getStartTime());
        contest.setEndTime(dto.getEndTime());
        contest.setFreezeBeforeMinutes(dto.getFreezeBeforeMinutes() == null ? contest.getFreezeBeforeMinutes() : dto.getFreezeBeforeMinutes());
        if (StringUtils.hasText(dto.getInviteCode())) {
            contest.setInviteCode(resolveInviteCode(dto.getInviteCode(), contest.getId()));
        }
        contest.setStatus(dto.getStatus() == null ? contest.getStatus() : dto.getStatus());
        contest.setUpdateBy(userId);
        contest.setUpdateTime(LocalDateTime.now());
        return this.updateById(contest);
    }

    public boolean deleteContest(Long id, Long userId) {
        requireContestAdmin(id, userId);
        return this.removeById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean registerContest(Long contestId, ContestRegisterDTO dto, Long userId) {
        Contest contest = requireContest(contestId);
        LocalDateTime now = LocalDateTime.now();
        if (contest.getStatus() != STATUS_PUBLISHED) {
            throw new BaseException(400, "Contest is not published");
        }
        if (now.isAfter(contest.getEndTime())) {
            throw new BaseException(400, "Contest has ended");
        }
        if (dto == null || !contest.getInviteCode().equals(dto.getInviteCode())) {
            throw new BaseException(400, "Invite code is invalid");
        }
        if (isRegistered(contestId, userId)) {
            return true;
        }
        ContestRegistration registration = new ContestRegistration();
        registration.setContestId(contestId);
        registration.setUserId(userId);
        registration.setCreateTime(now);
        contestRegistrationMapper.insert(registration);
        return true;
    }

    public boolean cancelRegistration(Long contestId, Long userId) {
        Contest contest = requireContest(contestId);
        if (!LocalDateTime.now().isBefore(contest.getStartTime())) {
            throw new BaseException(400, "Registration cannot be cancelled after contest starts");
        }
        return contestRegistrationMapper.delete(new LambdaQueryWrapper<ContestRegistration>()
                .eq(ContestRegistration::getContestId, contestId)
                .eq(ContestRegistration::getUserId, userId)) > 0;
    }

    public List<ContestRegistrationVO> listRegistrations(Long contestId, Long userId) {
        requireContestAdmin(contestId, userId);
        return selectRegistrations(contestId).stream().map(this::toRegistrationVO).toList();
    }

    public boolean removeRegistration(Long contestId, Long registeredUserId, Long operatorId) {
        requireContestAdmin(contestId, operatorId);
        return contestRegistrationMapper.delete(new LambdaQueryWrapper<ContestRegistration>()
                .eq(ContestRegistration::getContestId, contestId)
                .eq(ContestRegistration::getUserId, registeredUserId)) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean replaceContestProblems(Long contestId, List<ContestProblemDTO> problems, Long userId) {
        requireContestAdmin(contestId, userId);
        contestProblemMapper.delete(new LambdaQueryWrapper<ContestProblem>()
                .eq(ContestProblem::getContestId, contestId));
        if (!CollectionUtils.isEmpty(problems)) {
            insertContestProblems(contestId, problems);
        }
        touch(contestId, userId);
        return true;
    }

    public List<UserVO> listAdminCandidates() {
        ResponseResult<List<UserVO>> result = authUserFeignClient.listUsersByPermission(CONTEST_PERMISSION);
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            throw new BaseException(500, "Failed to query contest admin candidates");
        }
        return result.getData();
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean replaceAdmins(Long contestId, ContestAdminAssignDTO dto, Long operatorId) {
        Contest contest = requireContestAdmin(contestId, operatorId);
        Set<Long> nextAdmins = new HashSet<>();
        nextAdmins.add(contest.getCreatorId());
        if (dto != null && !CollectionUtils.isEmpty(dto.getUserIds())) {
            Set<Long> candidateIds = listAdminCandidates().stream().map(UserVO::getId).collect(Collectors.toSet());
            for (Long userId : dto.getUserIds()) {
                if (userId == null || userId.equals(contest.getCreatorId())) {
                    continue;
                }
                if (!candidateIds.contains(userId)) {
                    throw new BaseException(400, "User " + userId + " does not have CONTEST permission");
                }
                nextAdmins.add(userId);
            }
        }
        contestAdminMapper.delete(new LambdaQueryWrapper<ContestAdmin>()
                .eq(ContestAdmin::getContestId, contestId));
        nextAdmins.forEach(userId -> insertAdmin(contestId, userId, operatorId));
        return true;
    }

    public ContestSubmitCheckVO checkContestSubmit(Long contestId, Long problemId, Long userId) {
        Contest contest = this.getById(contestId);
        if (contest == null) {
            return new ContestSubmitCheckVO(false, null, "Contest does not exist");
        }
        if (contest.getStatus() != STATUS_PUBLISHED) {
            return new ContestSubmitCheckVO(false, null, "Contest is not published");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(contest.getStartTime())) {
            return new ContestSubmitCheckVO(false, null, "Contest has not started");
        }
        if (now.isAfter(contest.getEndTime())) {
            return new ContestSubmitCheckVO(false, null, "Contest has ended");
        }
        if (!isRegistered(contestId, userId)) {
            return new ContestSubmitCheckVO(false, null, "User is not registered for this contest");
        }
        ContestProblem contestProblem = selectContestProblem(contestId, problemId);
        if (contestProblem == null) {
            return new ContestSubmitCheckVO(false, null, "Problem is not in this contest");
        }
        return new ContestSubmitCheckVO(true, contestProblem.getId(), "OK");
    }

    public ContestRankVO getRank(Long contestId, Long userId) {
        Contest contest = requireContest(contestId);
        boolean admin = isAdmin(contest, userId);
        LocalDateTime freezeTime = getFreezeTime(contest);
        boolean frozen = freezeTime != null
                && !admin
                && !LocalDateTime.now().isBefore(freezeTime)
                && LocalDateTime.now().isBefore(contest.getEndTime());

        List<ContestRegistration> registrations = selectRegistrations(contestId);
        List<ContestProblem> problems = selectContestProblems(contestId);
        List<SubmissionVO> submissions = fetchContestSubmissions(contestId).stream()
                .filter(submission -> !frozen || submission.getCreateTime() == null || submission.getCreateTime().isBefore(freezeTime))
                .toList();

        List<ContestRankUserVO> users = switch (contest.getRuleType()) {
            case RULE_IOI -> buildScoreRank(contest, registrations, problems, submissions, false);
            case RULE_CODEFORCES -> buildCodeforcesRank(contest, registrations, problems, submissions);
            default -> buildAcmRank(contest, registrations, problems, submissions);
        };
        assignRanks(users);

        ContestRankVO vo = new ContestRankVO();
        vo.setContestId(contestId);
        vo.setRuleType(contest.getRuleType());
        vo.setFrozen(frozen);
        vo.setFreezeTime(freezeTime);
        vo.setRankings(users);
        return vo;
    }

    private List<ContestRankUserVO> buildAcmRank(Contest contest, List<ContestRegistration> registrations,
                                                  List<ContestProblem> problems, List<SubmissionVO> submissions) {
        Map<Long, List<SubmissionVO>> submissionsByUser = submissions.stream()
                .collect(Collectors.groupingBy(SubmissionVO::getUserId));
        List<ContestRankUserVO> users = registrations.stream()
                .map(registration -> {
                    ContestRankUserVO user = baseRankUser(registration.getUserId());
                    int solved = 0;
                    int penalty = 0;
                    LocalDateTime lastSubmitTime = null;
                    List<ContestRankProblemVO> problemRanks = new ArrayList<>();
                    for (ContestProblem problem : problems) {
                        List<SubmissionVO> problemSubmissions = submissionsForProblem(submissionsByUser.get(registration.getUserId()), problem);
                        ContestRankProblemVO problemRank = baseProblemRank(problem, problemSubmissions);
                        int wrongBeforeAccepted = 0;
                        LocalDateTime acceptedTime = null;
                        for (SubmissionVO submission : problemSubmissions) {
                            if (JUDGE_ACCEPTED == safeStatus(submission)) {
                                acceptedTime = submission.getCreateTime();
                                break;
                            }
                            if (WRONG_STATUSES.contains(safeStatus(submission))) {
                                wrongBeforeAccepted++;
                            }
                        }
                        if (acceptedTime != null) {
                            solved++;
                            int problemPenalty = minutesFromStart(contest, acceptedTime) + wrongBeforeAccepted * 20;
                            penalty += problemPenalty;
                            problemRank.setAccepted(true);
                            problemRank.setScore(1);
                            problemRank.setPenalty(problemPenalty);
                            problemRank.setWrongBeforeAccepted(wrongBeforeAccepted);
                            lastSubmitTime = maxTime(lastSubmitTime, acceptedTime);
                        }
                        problemRanks.add(problemRank);
                    }
                    user.setSolvedCount(solved);
                    user.setTotalScore(solved);
                    user.setPenalty(penalty);
                    user.setLastSubmitTime(lastSubmitTime);
                    user.setProblems(problemRanks);
                    return user;
                })
                .collect(Collectors.toCollection(ArrayList::new));
        users.sort(Comparator.comparing(ContestRankUserVO::getSolvedCount, Comparator.reverseOrder())
                .thenComparing(ContestRankUserVO::getPenalty)
                .thenComparing(ContestRankUserVO::getLastSubmitTime, NULLS_LAST_TIME)
                .thenComparing(ContestRankUserVO::getUserId));
        return users;
    }

    private List<ContestRankUserVO> buildScoreRank(Contest contest, List<ContestRegistration> registrations,
                                                   List<ContestProblem> problems, List<SubmissionVO> submissions,
                                                   boolean unused) {
        Map<Long, List<SubmissionVO>> submissionsByUser = submissions.stream()
                .collect(Collectors.groupingBy(SubmissionVO::getUserId));
        List<ContestRankUserVO> users = registrations.stream()
                .map(registration -> {
                    ContestRankUserVO user = baseRankUser(registration.getUserId());
                    int totalScore = 0;
                    int solved = 0;
                    LocalDateTime lastSubmitTime = null;
                    List<ContestRankProblemVO> problemRanks = new ArrayList<>();
                    for (ContestProblem problem : problems) {
                        List<SubmissionVO> problemSubmissions = submissionsForProblem(submissionsByUser.get(registration.getUserId()), problem);
                        ContestRankProblemVO problemRank = baseProblemRank(problem, problemSubmissions);
                        int bestScore = problemSubmissions.stream()
                                .mapToInt(submission -> scaleScore(submission.getScore(), problem.getScore()))
                                .max()
                                .orElse(0);
                        if (bestScore > 0) {
                            solved++;
                        }
                        totalScore += bestScore;
                        LocalDateTime lastProblemSubmit = latestSubmitTime(problemSubmissions);
                        lastSubmitTime = maxTime(lastSubmitTime, lastProblemSubmit);
                        problemRank.setAccepted(bestScore > 0);
                        problemRank.setScore(bestScore);
                        problemRanks.add(problemRank);
                    }
                    user.setSolvedCount(solved);
                    user.setTotalScore(totalScore);
                    user.setPenalty(0);
                    user.setLastSubmitTime(lastSubmitTime);
                    user.setProblems(problemRanks);
                    return user;
                })
                .collect(Collectors.toCollection(ArrayList::new));
        users.sort(Comparator.comparing(ContestRankUserVO::getTotalScore, Comparator.reverseOrder())
                .thenComparing(ContestRankUserVO::getLastSubmitTime, NULLS_LAST_TIME)
                .thenComparing(ContestRankUserVO::getUserId));
        return users;
    }

    private List<ContestRankUserVO> buildCodeforcesRank(Contest contest, List<ContestRegistration> registrations,
                                                        List<ContestProblem> problems, List<SubmissionVO> submissions) {
        Map<Long, List<SubmissionVO>> submissionsByUser = submissions.stream()
                .collect(Collectors.groupingBy(SubmissionVO::getUserId));
        long durationMinutes = Math.max(1, Duration.between(contest.getStartTime(), contest.getEndTime()).toMinutes());
        List<ContestRankUserVO> users = registrations.stream()
                .map(registration -> {
                    ContestRankUserVO user = baseRankUser(registration.getUserId());
                    int totalScore = 0;
                    int solved = 0;
                    LocalDateTime lastSubmitTime = null;
                    List<ContestRankProblemVO> problemRanks = new ArrayList<>();
                    for (ContestProblem problem : problems) {
                        List<SubmissionVO> problemSubmissions = submissionsForProblem(submissionsByUser.get(registration.getUserId()), problem);
                        ContestRankProblemVO problemRank = baseProblemRank(problem, problemSubmissions);
                        int wrongBeforeAccepted = 0;
                        LocalDateTime acceptedTime = null;
                        for (SubmissionVO submission : problemSubmissions) {
                            if (JUDGE_ACCEPTED == safeStatus(submission)) {
                                acceptedTime = submission.getCreateTime();
                                break;
                            }
                            if (WRONG_STATUSES.contains(safeStatus(submission))) {
                                wrongBeforeAccepted++;
                            }
                        }
                        if (acceptedTime != null) {
                            solved++;
                            int elapsed = minutesFromStart(contest, acceptedTime);
                            int score = codeforcesScore(problem.getScore(), elapsed, durationMinutes, wrongBeforeAccepted);
                            totalScore += score;
                            problemRank.setAccepted(true);
                            problemRank.setScore(score);
                            problemRank.setWrongBeforeAccepted(wrongBeforeAccepted);
                            lastSubmitTime = maxTime(lastSubmitTime, acceptedTime);
                        }
                        problemRanks.add(problemRank);
                    }
                    user.setSolvedCount(solved);
                    user.setTotalScore(totalScore);
                    user.setPenalty(0);
                    user.setLastSubmitTime(lastSubmitTime);
                    user.setProblems(problemRanks);
                    return user;
                })
                .collect(Collectors.toCollection(ArrayList::new));
        users.sort(Comparator.comparing(ContestRankUserVO::getTotalScore, Comparator.reverseOrder())
                .thenComparing(ContestRankUserVO::getLastSubmitTime, NULLS_LAST_TIME)
                .thenComparing(ContestRankUserVO::getUserId));
        return users;
    }

    private void insertContestProblems(Long contestId, List<ContestProblemDTO> problems) {
        List<ContestProblemDTO> normalized = normalizeContestProblems(problems);
        validateProblemIds(normalized.stream().map(ContestProblemDTO::getProblemId).toList());
        for (ContestProblemDTO dto : normalized) {
            ContestProblem contestProblem = new ContestProblem();
            contestProblem.setContestId(contestId);
            contestProblem.setProblemId(dto.getProblemId());
            contestProblem.setLabel(dto.getLabel());
            contestProblem.setSortOrder(dto.getSortOrder());
            contestProblem.setScore(dto.getScore());
            contestProblem.setCreateTime(LocalDateTime.now());
            contestProblemMapper.insert(contestProblem);
        }
    }

    private List<ContestProblemDTO> normalizeContestProblems(List<ContestProblemDTO> problems) {
        Map<Long, ContestProblemDTO> unique = new LinkedHashMap<>();
        int index = 1;
        for (ContestProblemDTO item : problems) {
            if (item == null || item.getProblemId() == null) {
                throw new BadRequestException("problemId cannot be empty");
            }
            if (!unique.containsKey(item.getProblemId())) {
                if (!StringUtils.hasText(item.getLabel())) {
                    item.setLabel(defaultProblemLabel(index));
                }
                if (item.getSortOrder() == null) {
                    item.setSortOrder(index);
                }
                if (item.getScore() == null || item.getScore() <= 0) {
                    item.setScore(100);
                }
                unique.put(item.getProblemId(), item);
                index++;
            }
        }
        return new ArrayList<>(unique.values());
    }

    private void validateProblemIds(List<Long> problemIds) {
        if (problemIds.isEmpty()) {
            return;
        }
        List<Problem> existing = problemMapper.selectBatchIds(problemIds);
        if (existing.size() != problemIds.size()) {
            throw new BadRequestException("Some problems do not exist");
        }
    }

    private Contest requireContest(Long contestId) {
        Contest contest = this.getById(contestId);
        if (contest == null) {
            throw new BaseException(404, "Contest does not exist");
        }
        return contest;
    }

    private Contest requireContestAdmin(Long contestId, Long userId) {
        Contest contest = requireContest(contestId);
        if (!isAdmin(contest, userId)) {
            throw new BaseException(403, "Only contest admins can perform this action");
        }
        return contest;
    }

    private boolean canView(Contest contest, Long userId) {
        return contest.getStatus() == STATUS_PUBLISHED || isAdmin(contest, userId);
    }

    private boolean isAdmin(Contest contest, Long userId) {
        if (userId == null || contest == null) {
            return false;
        }
        if (userId.equals(contest.getCreatorId())) {
            return true;
        }
        return contestAdminMapper.selectCount(new LambdaQueryWrapper<ContestAdmin>()
                .eq(ContestAdmin::getContestId, contest.getId())
                .eq(ContestAdmin::getUserId, userId)) > 0;
    }

    private boolean isRegistered(Long contestId, Long userId) {
        return contestRegistrationMapper.selectCount(new LambdaQueryWrapper<ContestRegistration>()
                .eq(ContestRegistration::getContestId, contestId)
                .eq(ContestRegistration::getUserId, userId)) > 0;
    }

    private void insertAdmin(Long contestId, Long userId, Long operatorId) {
        if (contestAdminMapper.selectCount(new LambdaQueryWrapper<ContestAdmin>()
                .eq(ContestAdmin::getContestId, contestId)
                .eq(ContestAdmin::getUserId, userId)) > 0) {
            return;
        }
        ContestAdmin admin = new ContestAdmin();
        admin.setContestId(contestId);
        admin.setUserId(userId);
        admin.setCreateBy(operatorId);
        admin.setCreateTime(LocalDateTime.now());
        contestAdminMapper.insert(admin);
    }

    private void touch(Long contestId, Long userId) {
        Contest update = new Contest();
        update.setId(contestId);
        update.setUpdateBy(userId);
        update.setUpdateTime(LocalDateTime.now());
        this.updateById(update);
    }

    private String resolveInviteCode(String inviteCode, Long excludeContestId) {
        String code = StringUtils.hasText(inviteCode) ? inviteCode : generateInviteCode();
        validateInviteCode(code);
        LambdaQueryWrapper<Contest> wrapper = new LambdaQueryWrapper<Contest>()
                .eq(Contest::getInviteCode, code)
                .ne(excludeContestId != null, Contest::getId, excludeContestId);
        if (this.count(wrapper) > 0) {
            throw new BaseException(400, "Invite code already exists");
        }
        return code;
    }

    private String generateInviteCode() {
        for (int attempt = 0; attempt < 20; attempt++) {
            StringBuilder builder = new StringBuilder(INVITE_LENGTH);
            for (int i = 0; i < INVITE_LENGTH; i++) {
                builder.append(INVITE_CHARACTERS.charAt(secureRandom.nextInt(INVITE_CHARACTERS.length())));
            }
            String code = builder.toString();
            if (this.count(new LambdaQueryWrapper<Contest>().eq(Contest::getInviteCode, code)) == 0) {
                return code;
            }
        }
        throw new BaseException(500, "Failed to generate invite code");
    }

    private void validateInviteCode(String inviteCode) {
        if (inviteCode == null || inviteCode.length() != INVITE_LENGTH) {
            throw new BadRequestException("Invite code must be 10 characters");
        }
        for (char c : inviteCode.toCharArray()) {
            if (INVITE_CHARACTERS.indexOf(c) < 0) {
                throw new BadRequestException("Invite code contains unsupported character");
            }
        }
    }

    private void validateContestDTO(ContestSaveDTO dto, boolean requireId) {
        if (dto == null) {
            throw new BadRequestException("Request body cannot be empty");
        }
        if (requireId && dto.getId() == null) {
            throw new BadRequestException("Contest id cannot be empty");
        }
        if (!StringUtils.hasText(dto.getTitle())) {
            throw new BadRequestException("Contest title cannot be empty");
        }
        if (dto.getRuleType() == null || dto.getRuleType() < RULE_ACM || dto.getRuleType() > RULE_CODEFORCES) {
            throw new BadRequestException("Contest rule type must be 1, 2, or 3");
        }
        if (dto.getStartTime() == null || dto.getEndTime() == null || !dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new BadRequestException("Contest time range is invalid");
        }
        if (dto.getFreezeBeforeMinutes() != null && dto.getFreezeBeforeMinutes() < 0) {
            throw new BadRequestException("Freeze minutes cannot be negative");
        }
        if (dto.getStatus() != null && dto.getStatus() != STATUS_DRAFT
                && dto.getStatus() != STATUS_PUBLISHED && dto.getStatus() != STATUS_CANCELLED) {
            throw new BadRequestException("Contest status must be 0, 1, or 2");
        }
        if (StringUtils.hasText(dto.getInviteCode())) {
            validateInviteCode(dto.getInviteCode());
        }
    }

    private ContestProblem selectContestProblem(Long contestId, Long problemId) {
        return contestProblemMapper.selectOne(new LambdaQueryWrapper<ContestProblem>()
                .eq(ContestProblem::getContestId, contestId)
                .eq(ContestProblem::getProblemId, problemId)
                .last("LIMIT 1"));
    }

    private List<ContestProblem> selectContestProblems(Long contestId) {
        return contestProblemMapper.selectList(new LambdaQueryWrapper<ContestProblem>()
                .eq(ContestProblem::getContestId, contestId)
                .orderByAsc(ContestProblem::getSortOrder)
                .orderByAsc(ContestProblem::getId));
    }

    private List<ContestRegistration> selectRegistrations(Long contestId) {
        return contestRegistrationMapper.selectList(new LambdaQueryWrapper<ContestRegistration>()
                .eq(ContestRegistration::getContestId, contestId)
                .orderByAsc(ContestRegistration::getCreateTime)
                .orderByAsc(ContestRegistration::getId));
    }

    private List<Long> selectAdminUserIds(Long contestId) {
        return contestAdminMapper.selectList(new LambdaQueryWrapper<ContestAdmin>()
                        .eq(ContestAdmin::getContestId, contestId)
                        .orderByAsc(ContestAdmin::getId))
                .stream()
                .map(ContestAdmin::getUserId)
                .toList();
    }

    private List<SubmissionVO> fetchContestSubmissions(Long contestId) {
        ResponseResult<List<SubmissionVO>> result = submissionFeignClient.listContestSubmissions(contestId);
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            throw new BaseException(500, "Failed to query contest submissions");
        }
        return result.getData();
    }

    private ContestVO toVO(Contest contest, boolean withProblems, Long userId) {
        ContestVO vo = new ContestVO();
        BeanUtils.copyProperties(contest, vo);
        boolean admin = isAdmin(contest, userId);
        vo.setAdmin(admin);
        vo.setRegistered(userId != null && isRegistered(contest.getId(), userId));
        vo.setRuleTypeDesc(ruleDesc(contest.getRuleType()));
        vo.setRegistrationCount(Math.toIntExact(contestRegistrationMapper.selectCount(
                new LambdaQueryWrapper<ContestRegistration>().eq(ContestRegistration::getContestId, contest.getId()))));
        vo.setAdminUserIds(selectAdminUserIds(contest.getId()));
        if (!admin) {
            vo.setInviteCode(null);
        }
        if (withProblems) {
            vo.setProblems(selectContestProblemVOs(contest.getId()));
        }
        return vo;
    }

    private List<ContestProblemVO> selectContestProblemVOs(Long contestId) {
        return selectContestProblems(contestId).stream().map(contestProblem -> {
            ContestProblemVO vo = new ContestProblemVO();
            BeanUtils.copyProperties(contestProblem, vo);
            vo.setProblem(problemService.getProblemDetail(contestProblem.getProblemId()));
            return vo;
        }).toList();
    }

    private ContestRegistrationVO toRegistrationVO(ContestRegistration registration) {
        ContestRegistrationVO vo = new ContestRegistrationVO();
        BeanUtils.copyProperties(registration, vo);
        return vo;
    }

    private List<SubmissionVO> submissionsForProblem(List<SubmissionVO> submissions, ContestProblem problem) {
        if (submissions == null || submissions.isEmpty()) {
            return List.of();
        }
        return submissions.stream()
                .filter(submission -> problem.getId().equals(submission.getContestProblemId())
                        || (submission.getContestProblemId() == null && problem.getProblemId().equals(submission.getProblemId())))
                .sorted(Comparator.comparing(SubmissionVO::getCreateTime, NULLS_LAST_TIME)
                        .thenComparing(SubmissionVO::getId))
                .toList();
    }

    private ContestRankUserVO baseRankUser(Long userId) {
        ContestRankUserVO user = new ContestRankUserVO();
        user.setUserId(userId);
        user.setSolvedCount(0);
        user.setTotalScore(0);
        user.setPenalty(0);
        user.setProblems(List.of());
        return user;
    }

    private ContestRankProblemVO baseProblemRank(ContestProblem problem, List<SubmissionVO> submissions) {
        ContestRankProblemVO vo = new ContestRankProblemVO();
        vo.setContestProblemId(problem.getId());
        vo.setProblemId(problem.getProblemId());
        vo.setLabel(problem.getLabel());
        vo.setAccepted(false);
        vo.setScore(0);
        vo.setPenalty(0);
        vo.setSubmissionCount(submissions == null ? 0 : submissions.size());
        vo.setWrongBeforeAccepted(0);
        vo.setLastSubmitTime(latestSubmitTime(submissions));
        return vo;
    }

    private int safeStatus(SubmissionVO submission) {
        return submission.getStatus() == null ? -1 : submission.getStatus();
    }

    private int scaleScore(Integer judgeScore, Integer baseScore) {
        int score = judgeScore == null ? 0 : judgeScore;
        int base = baseScore == null ? 100 : baseScore;
        return (int) Math.round(score * base / 100.0);
    }

    private int codeforcesScore(Integer baseScore, int elapsedMinutes, long durationMinutes, int wrongBeforeAccepted) {
        int base = baseScore == null ? 100 : baseScore;
        double timeScore = base * (1 - elapsedMinutes / (durationMinutes * 2.0)) - wrongBeforeAccepted * 50;
        double lowerBound = base * 0.3;
        return (int) Math.floor(Math.max(lowerBound, timeScore));
    }

    private int minutesFromStart(Contest contest, LocalDateTime time) {
        if (time == null || time.isBefore(contest.getStartTime())) {
            return 0;
        }
        return (int) Duration.between(contest.getStartTime(), time).toMinutes();
    }

    private LocalDateTime latestSubmitTime(List<SubmissionVO> submissions) {
        if (submissions == null || submissions.isEmpty()) {
            return null;
        }
        return submissions.stream()
                .map(SubmissionVO::getCreateTime)
                .filter(time -> time != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    private LocalDateTime maxTime(LocalDateTime left, LocalDateTime right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.isAfter(right) ? left : right;
    }

    private LocalDateTime getFreezeTime(Contest contest) {
        if (contest.getFreezeBeforeMinutes() == null || contest.getFreezeBeforeMinutes() <= 0) {
            return null;
        }
        return contest.getEndTime().minusMinutes(contest.getFreezeBeforeMinutes());
    }

    private void assignRanks(List<ContestRankUserVO> users) {
        int rank = 1;
        for (ContestRankUserVO user : users) {
            user.setRank(rank++);
        }
    }

    private String defaultProblemLabel(int index) {
        if (index <= 26) {
            return String.valueOf((char) ('A' + index - 1));
        }
        return "P" + index;
    }

    private String ruleDesc(Integer ruleType) {
        return switch (ruleType == null ? RULE_ACM : ruleType) {
            case RULE_IOI -> "IOI";
            case RULE_CODEFORCES -> "Codeforces";
            default -> "ACM/ICPC";
        };
    }
}
