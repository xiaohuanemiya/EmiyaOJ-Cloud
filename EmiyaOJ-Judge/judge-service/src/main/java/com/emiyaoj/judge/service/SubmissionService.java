package com.emiyaoj.judge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.emiyaoj.common.domain.PageDTO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.common.exception.BaseException;
import com.emiyaoj.judge.domain.JudgeStatus;
import com.emiyaoj.judge.domain.entity.Submission;
import com.emiyaoj.judge.domain.entity.SubmissionCaseResult;
import com.emiyaoj.judge.domain.entity.SubmissionJudgeResult;
import com.emiyaoj.judge.dto.DifficultySolvedStatsVO;
import com.emiyaoj.judge.dto.JudgeUserStatsVO;
import com.emiyaoj.judge.dto.SolvedProblemVO;
import com.emiyaoj.judge.dto.SubmissionCaseResultVO;
import com.emiyaoj.judge.dto.SubmissionDetailVO;
import com.emiyaoj.judge.dto.SubmissionVO;
import com.emiyaoj.judge.dto.SubmitCodeDTO;
import com.emiyaoj.judge.mapper.SubmissionCaseResultMapper;
import com.emiyaoj.judge.mapper.SubmissionJudgeResultMapper;
import com.emiyaoj.judge.mapper.SubmissionMapper;
import com.emiyaoj.problem.api.ProblemFeignClient;
import com.emiyaoj.problem.dto.ContestSubmitCheckVO;
import com.emiyaoj.problem.dto.LanguageVO;
import com.emiyaoj.problem.dto.ProblemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionMapper submissionMapper;
    private final SubmissionJudgeResultMapper judgeResultMapper;
    private final SubmissionCaseResultMapper caseResultMapper;
    private final JudgeExecutor judgeExecutor;
    private final ProblemFeignClient problemFeignClient;

    public SubmissionVO submitCode(SubmitCodeDTO dto, Long userId) {
        ResponseResult<ProblemVO> problemResult = problemFeignClient.getProblemById(dto.getProblemId());
        if (problemResult == null || problemResult.getCode() != 200 || problemResult.getData() == null) {
            throw new BaseException(400, "Problem does not exist");
        }

        ResponseResult<LanguageVO> languageResult = problemFeignClient.getLanguageById(dto.getLanguageId());
        if (languageResult == null || languageResult.getCode() != 200 || languageResult.getData() == null) {
            throw new BaseException(400, "Language does not exist");
        }

        Long contestProblemId = null;
        if (dto.getContestId() != null) {
            ResponseResult<ContestSubmitCheckVO> checkResult = problemFeignClient.checkContestSubmit(
                    dto.getContestId(), dto.getProblemId(), userId);
            if (checkResult == null || checkResult.getCode() != 200 || checkResult.getData() == null
                    || !Boolean.TRUE.equals(checkResult.getData().getAllowed())) {
                String message = checkResult != null && checkResult.getData() != null
                        ? checkResult.getData().getMessage()
                        : "Contest submission is not allowed";
                throw new BaseException(400, message);
            }
            contestProblemId = checkResult.getData().getContestProblemId();
        }

        Submission submission = new Submission();
        submission.setProblemId(dto.getProblemId());
        submission.setContestId(dto.getContestId());
        submission.setContestProblemId(contestProblemId);
        submission.setUserId(userId);
        submission.setLanguageId(dto.getLanguageId());
        submission.setCode(dto.getCode());
        submission.setCreateTime(LocalDateTime.now());
        submission.setUpdateTime(LocalDateTime.now());
        submission.setDeleted(0);
        submissionMapper.insert(submission);

        SubmissionJudgeResult judgeResult = new SubmissionJudgeResult();
        judgeResult.setSubmissionId(submission.getId());
        judgeResult.setStatus(JudgeStatus.PENDING);
        judgeResult.setPassedCaseCount(0);
        judgeResult.setTotalCaseCount(0);
        judgeResult.setScore(0);
        judgeResult.setMaxTimeUsed(0L);
        judgeResult.setMaxMemoryUsed(0L);
        judgeResultMapper.insert(judgeResult);

        log.info("Submission created: id={}, contestId={}", submission.getId(), submission.getContestId());
        judgeExecutor.executeJudgeAsync(submission.getId(), dto.getProblemId(), dto.getLanguageId(), dto.getCode());

        return toSubmissionVO(submission, judgeResult);
    }

    public SubmissionDetailVO getSubmissionById(Long id) {
        Submission submission = submissionMapper.selectById(id);
        if (submission == null) {
            return null;
        }

        SubmissionJudgeResult judgeResult = selectJudgeResult(id);
        List<SubmissionCaseResult> caseResults = caseResultMapper.selectList(
                new LambdaQueryWrapper<SubmissionCaseResult>()
                        .eq(SubmissionCaseResult::getSubmissionId, id)
                        .orderByAsc(SubmissionCaseResult::getCaseOrder)
                        .orderByAsc(SubmissionCaseResult::getId)
        );

        SubmissionDetailVO vo = new SubmissionDetailVO();
        BeanUtils.copyProperties(toSubmissionVO(submission, judgeResult), vo);
        vo.setCaseResults(caseResults.stream().map(this::toCaseResultVO).toList());
        return vo;
    }

    public PageVO<SubmissionVO> getSubmissionPage(PageDTO pageDTO, Long problemId, Long userId) {
        LambdaQueryWrapper<Submission> wrapper = new LambdaQueryWrapper<>();
        if (problemId != null) {
            wrapper.eq(Submission::getProblemId, problemId);
        }
        if (userId != null) {
            wrapper.eq(Submission::getUserId, userId);
        }
        wrapper.orderByDesc(Submission::getCreateTime);

        Page<Submission> page = submissionMapper.selectPage(
                new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize()),
                wrapper
        );

        Map<Long, SubmissionJudgeResult> resultMap = selectJudgeResultMap(page.getRecords());
        List<SubmissionVO> voList = page.getRecords().stream()
                .map(submission -> toSubmissionVO(submission, resultMap.get(submission.getId())))
                .toList();

        return new PageVO<>(page.getTotal(), voList, (long) pageDTO.getPageNum(), (long) pageDTO.getPageSize());
    }

    public List<SubmissionVO> listContestSubmissions(Long contestId) {
        List<Submission> submissions = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getContestId, contestId)
                        .orderByAsc(Submission::getCreateTime)
                        .orderByAsc(Submission::getId)
        );
        Map<Long, SubmissionJudgeResult> resultMap = selectJudgeResultMap(submissions);
        return submissions.stream()
                .map(submission -> toSubmissionVO(submission, resultMap.get(submission.getId())))
                .toList();
    }

    public JudgeUserStatsVO getUserStats(Long userId) {
        int totalSubmitCount = valueOrZero(submissionMapper.countUserSubmissions(userId));
        int acceptedSubmitCount = valueOrZero(
                submissionMapper.countUserSubmissionsByStatus(userId, JudgeStatus.ACCEPTED));
        List<SolvedProblemVO> solvedProblems = listPublicSolvedProblems(userId);

        Map<Integer, Long> countByDifficulty = solvedProblems.stream()
                .filter(problem -> problem.getDifficulty() != null)
                .collect(Collectors.groupingBy(SolvedProblemVO::getDifficulty, Collectors.counting()));
        List<DifficultySolvedStatsVO> difficultyStats = List.of(1, 2, 3).stream()
                .map(difficulty -> new DifficultySolvedStatsVO(
                        difficulty,
                        difficultyDesc(difficulty),
                        Math.toIntExact(countByDifficulty.getOrDefault(difficulty, 0L))))
                .toList();

        JudgeUserStatsVO stats = new JudgeUserStatsVO();
        stats.setUserId(userId);
        stats.setSolvedCount(solvedProblems.size());
        stats.setTotalSubmitCount(totalSubmitCount);
        stats.setAcceptedSubmitCount(acceptedSubmitCount);
        stats.setPassRate(calculatePassRate(acceptedSubmitCount, totalSubmitCount));
        stats.setDifficultyStats(difficultyStats);
        return stats;
    }

    public PageVO<SolvedProblemVO> getSolvedProblems(PageDTO pageDTO, Long userId, Integer difficulty) {
        int pageNum = pageDTO.getPageNum() == null || pageDTO.getPageNum() < 1 ? 1 : pageDTO.getPageNum();
        int pageSize = pageDTO.getPageSize() == null || pageDTO.getPageSize() < 1 ? 10 : pageDTO.getPageSize();

        List<SolvedProblemVO> filtered = listPublicSolvedProblems(userId).stream()
                .filter(problem -> difficulty == null || difficulty.equals(problem.getDifficulty()))
                .sorted(Comparator.comparing(SolvedProblemVO::getAcceptedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        int fromIndex = Math.min((pageNum - 1) * pageSize, filtered.size());
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());
        return new PageVO<>((long) filtered.size(), filtered.subList(fromIndex, toIndex), (long) pageNum, (long) pageSize);
    }

    private SubmissionJudgeResult selectJudgeResult(Long submissionId) {
        return judgeResultMapper.selectOne(
                new LambdaQueryWrapper<SubmissionJudgeResult>()
                        .eq(SubmissionJudgeResult::getSubmissionId, submissionId)
                        .last("LIMIT 1")
        );
    }

    private Map<Long, SubmissionJudgeResult> selectJudgeResultMap(List<Submission> submissions) {
        if (submissions == null || submissions.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> submissionIds = submissions.stream().map(Submission::getId).toList();
        return judgeResultMapper.selectList(
                        new LambdaQueryWrapper<SubmissionJudgeResult>()
                                .in(SubmissionJudgeResult::getSubmissionId, submissionIds)
                ).stream()
                .collect(Collectors.toMap(
                        SubmissionJudgeResult::getSubmissionId,
                        Function.identity(),
                        (left, right) -> left
                ));
    }

    private List<SolvedProblemVO> listPublicSolvedProblems(Long userId) {
        List<SolvedProblemVO> acceptedProblems = submissionMapper.listUserAcceptedProblems(userId, JudgeStatus.ACCEPTED);
        if (acceptedProblems.isEmpty()) {
            return List.of();
        }

        List<Long> problemIds = acceptedProblems.stream()
                .map(SolvedProblemVO::getProblemId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, ProblemVO> problemsById = loadPublicProblemsByIds(problemIds);
        List<SolvedProblemVO> result = new ArrayList<>();
        for (SolvedProblemVO acceptedProblem : acceptedProblems) {
            ProblemVO problem = problemsById.get(acceptedProblem.getProblemId());
            if (problem == null) {
                continue;
            }
            acceptedProblem.setTitle(problem.getTitle());
            acceptedProblem.setDifficulty(problem.getDifficulty());
            acceptedProblem.setDifficultyDesc(problem.getDifficultyDesc());
            result.add(acceptedProblem);
        }
        return result;
    }

    private Map<Long, ProblemVO> loadPublicProblemsByIds(List<Long> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            return Map.of();
        }
        try {
            ResponseResult<List<ProblemVO>> result = problemFeignClient.listPublicProblemsByIds(problemIds);
            if (result == null || result.getCode() != 200 || result.getData() == null) {
                return Map.of();
            }
            return result.getData().stream()
                    .filter(problem -> problem.getId() != null)
                    .collect(Collectors.toMap(ProblemVO::getId, Function.identity(), (left, right) -> left));
        } catch (Exception e) {
            log.warn("load public problems failed, ids={}", problemIds, e);
            return Map.of();
        }
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal calculatePassRate(int acceptedSubmitCount, int totalSubmitCount) {
        if (totalSubmitCount <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(acceptedSubmitCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalSubmitCount), 2, RoundingMode.HALF_UP);
    }

    private String difficultyDesc(Integer difficulty) {
        if (difficulty == null) {
            return "未知";
        }
        return switch (difficulty) {
            case 1 -> "简单";
            case 2 -> "中等";
            case 3 -> "困难";
            default -> "未知";
        };
    }

    private SubmissionVO toSubmissionVO(Submission submission, SubmissionJudgeResult judgeResult) {
        SubmissionVO vo = new SubmissionVO();
        vo.setId(submission.getId());
        vo.setProblemId(submission.getProblemId());
        vo.setContestId(submission.getContestId());
        vo.setContestProblemId(submission.getContestProblemId());
        vo.setUserId(submission.getUserId());
        vo.setLanguageId(submission.getLanguageId());
        vo.setCreateTime(submission.getCreateTime());

        if (judgeResult == null) {
            vo.setStatus(JudgeStatus.PENDING);
            vo.setPassedCaseCount(0);
            vo.setTotalCaseCount(0);
            vo.setScore(0);
            vo.setMaxTimeUsed(0L);
            vo.setMaxMemoryUsed(0L);
            return vo;
        }

        vo.setStatus(judgeResult.getStatus());
        vo.setPassedCaseCount(judgeResult.getPassedCaseCount());
        vo.setTotalCaseCount(judgeResult.getTotalCaseCount());
        vo.setScore(judgeResult.getScore());
        vo.setMaxTimeUsed(judgeResult.getMaxTimeUsed());
        vo.setMaxMemoryUsed(judgeResult.getMaxMemoryUsed());
        vo.setErrorMessage(judgeResult.getErrorMessage());
        vo.setCompileMessage(judgeResult.getCompileMessage());
        vo.setFinishTime(judgeResult.getFinishTime());
        return vo;
    }

    private SubmissionCaseResultVO toCaseResultVO(SubmissionCaseResult result) {
        SubmissionCaseResultVO vo = new SubmissionCaseResultVO();
        BeanUtils.copyProperties(result, vo);
        return vo;
    }
}
