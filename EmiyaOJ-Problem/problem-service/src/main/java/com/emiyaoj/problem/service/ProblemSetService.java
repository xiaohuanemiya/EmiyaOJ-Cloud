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
import com.emiyaoj.problem.domain.pojo.Problem;
import com.emiyaoj.problem.domain.pojo.ProblemSet;
import com.emiyaoj.problem.domain.pojo.ProblemSetProblem;
import com.emiyaoj.problem.dto.ProblemSetProblemDTO;
import com.emiyaoj.problem.dto.ProblemSetProblemVO;
import com.emiyaoj.problem.dto.ProblemSetQueryDTO;
import com.emiyaoj.problem.dto.ProblemSetSaveDTO;
import com.emiyaoj.problem.dto.ProblemSetVO;
import com.emiyaoj.problem.dto.ProblemVO;
import com.emiyaoj.problem.mapper.ProblemMapper;
import com.emiyaoj.problem.mapper.ProblemSetMapper;
import com.emiyaoj.problem.mapper.ProblemSetProblemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemSetService extends ServiceImpl<ProblemSetMapper, ProblemSet> {

    private final ProblemSetProblemMapper problemSetProblemMapper;
    private final ProblemMapper problemMapper;
    private final ProblemService problemService;
    private final AuthUserFeignClient authUserFeignClient;

    public PageVO<ProblemSetVO> queryProblemSetPage(ProblemSetQueryDTO queryDTO, Long userId) {
        Page<ProblemSet> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<ProblemSet> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getTitle()), ProblemSet::getTitle, queryDTO.getTitle())
                .eq(queryDTO.getStatus() != null, ProblemSet::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getCreatorId() != null, ProblemSet::getCreatorId, queryDTO.getCreatorId())
                .and(userId != null, w -> w.eq(ProblemSet::getStatus, 1).or().eq(ProblemSet::getCreatorId, userId))
                .eq(userId == null, ProblemSet::getStatus, 1)
                .orderByDesc(ProblemSet::getCreateTime);
        this.page(page, wrapper);
        Map<Long, UserVO> usersById = loadUsersByIds(page.getRecords().stream().map(ProblemSet::getCreatorId).toList());
        return PageVO.of(page, problemSet -> toVO(problemSet, false, userId, usersById));
    }

    public ProblemSetVO getProblemSetDetail(Long id, Long userId) {
        ProblemSet problemSet = this.getById(id);
        if (problemSet == null || !isVisible(problemSet, userId)) {
            return null;
        }
        Map<Long, UserVO> usersById = loadUsersByIds(problemSet.getCreatorId() == null ? List.of() : List.of(problemSet.getCreatorId()));
        return toVO(problemSet, true, userId, usersById);
    }

    @Transactional(rollbackFor = Exception.class)
    public ProblemSetVO saveProblemSet(ProblemSetSaveDTO dto, Long userId) {
        validateSaveDTO(dto, false);
        ProblemSet problemSet = new ProblemSet();
        BeanUtils.copyProperties(dto, problemSet);
        problemSet.setCreatorId(userId);
        problemSet.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        problemSet.setDeleted(0);
        problemSet.setCreateBy(userId);
        problemSet.setUpdateBy(userId);
        problemSet.setCreateTime(LocalDateTime.now());
        problemSet.setUpdateTime(LocalDateTime.now());
        this.save(problemSet);

        if (!CollectionUtils.isEmpty(dto.getProblems())) {
            replaceProblemAssociations(problemSet.getId(), dto.getProblems());
        }
        Map<Long, UserVO> usersById = loadUsersByIds(problemSet.getCreatorId() == null ? List.of() : List.of(problemSet.getCreatorId()));
        return toVO(problemSet, true, userId, usersById);
    }

    public boolean updateProblemSet(ProblemSetSaveDTO dto, Long userId) {
        validateSaveDTO(dto, true);
        ProblemSet existing = requireOwner(dto.getId(), userId);
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setStatus(dto.getStatus() == null ? existing.getStatus() : dto.getStatus());
        existing.setUpdateBy(userId);
        existing.setUpdateTime(LocalDateTime.now());
        return this.updateById(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProblemSet(Long id, Long userId) {
        requireOwner(id, userId);
        problemSetProblemMapper.delete(new LambdaQueryWrapper<ProblemSetProblem>()
                .eq(ProblemSetProblem::getSetId, id));
        return this.removeById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean replaceProblems(Long setId, List<ProblemSetProblemDTO> problems, Long userId) {
        requireOwner(setId, userId);
        problemSetProblemMapper.delete(new LambdaQueryWrapper<ProblemSetProblem>()
                .eq(ProblemSetProblem::getSetId, setId));
        if (!CollectionUtils.isEmpty(problems)) {
            replaceProblemAssociations(setId, problems);
        }
        touch(setId, userId);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean appendProblems(Long setId, List<ProblemSetProblemDTO> problems, Long userId) {
        requireOwner(setId, userId);
        if (CollectionUtils.isEmpty(problems)) {
            return true;
        }

        validateProblemIds(problems);
        for (ProblemSetProblemDTO dto : normalizeProblemDTOs(problems)) {
            ProblemSetProblem existing = problemSetProblemMapper.selectOne(new LambdaQueryWrapper<ProblemSetProblem>()
                    .eq(ProblemSetProblem::getSetId, setId)
                    .eq(ProblemSetProblem::getProblemId, dto.getProblemId())
                    .last("LIMIT 1"));
            if (existing == null) {
                problemSetProblemMapper.insert(toAssociation(setId, dto));
            } else {
                existing.setSortOrder(dto.getSortOrder());
                existing.setNote(dto.getNote());
                problemSetProblemMapper.updateById(existing);
            }
        }
        touch(setId, userId);
        return true;
    }

    public boolean removeProblem(Long setId, Long problemId, Long userId) {
        requireOwner(setId, userId);
        int deleted = problemSetProblemMapper.delete(new LambdaQueryWrapper<ProblemSetProblem>()
                .eq(ProblemSetProblem::getSetId, setId)
                .eq(ProblemSetProblem::getProblemId, problemId));
        touch(setId, userId);
        return deleted > 0;
    }

    private void replaceProblemAssociations(Long setId, List<ProblemSetProblemDTO> problems) {
        validateProblemIds(problems);
        for (ProblemSetProblemDTO dto : normalizeProblemDTOs(problems)) {
            problemSetProblemMapper.insert(toAssociation(setId, dto));
        }
    }

    private ProblemSetProblem toAssociation(Long setId, ProblemSetProblemDTO dto) {
        ProblemSetProblem association = new ProblemSetProblem();
        association.setSetId(setId);
        association.setProblemId(dto.getProblemId());
        association.setSortOrder(dto.getSortOrder());
        association.setNote(dto.getNote());
        association.setCreateTime(LocalDateTime.now());
        return association;
    }

    private List<ProblemSetProblemDTO> normalizeProblemDTOs(List<ProblemSetProblemDTO> problems) {
        Map<Long, ProblemSetProblemDTO> unique = new LinkedHashMap<>();
        int index = 1;
        for (ProblemSetProblemDTO item : problems) {
            if (item == null || item.getProblemId() == null) {
                throw new BadRequestException("problemId cannot be empty");
            }
            if (!unique.containsKey(item.getProblemId())) {
                if (item.getSortOrder() == null) {
                    item.setSortOrder(index);
                }
                unique.put(item.getProblemId(), item);
                index++;
            }
        }
        return new ArrayList<>(unique.values());
    }

    private void validateProblemIds(List<ProblemSetProblemDTO> problems) {
        List<Long> problemIds = normalizeProblemDTOs(problems).stream()
                .map(ProblemSetProblemDTO::getProblemId)
                .toList();
        if (problemIds.isEmpty()) {
            return;
        }
        List<Problem> existing = problemMapper.selectBatchIds(problemIds);
        if (existing.size() != problemIds.size()) {
            throw new BadRequestException("Some problems do not exist");
        }
    }

    private ProblemSet requireOwner(Long setId, Long userId) {
        ProblemSet problemSet = this.getById(setId);
        if (problemSet == null) {
            throw new BaseException(404, "Problem set does not exist");
        }
        if (!problemSet.getCreatorId().equals(userId)) {
            throw new BaseException(403, "Only the creator can manage this problem set");
        }
        return problemSet;
    }

    private void touch(Long setId, Long userId) {
        ProblemSet update = new ProblemSet();
        update.setId(setId);
        update.setUpdateBy(userId);
        update.setUpdateTime(LocalDateTime.now());
        this.updateById(update);
    }

    private void validateSaveDTO(ProblemSetSaveDTO dto, boolean requireId) {
        if (dto == null) {
            throw new BadRequestException("Request body cannot be empty");
        }
        if (requireId && dto.getId() == null) {
            throw new BadRequestException("Problem set id cannot be empty");
        }
        if (!StringUtils.hasText(dto.getTitle())) {
            throw new BadRequestException("Problem set title cannot be empty");
        }
        if (dto.getStatus() != null && dto.getStatus() != 0 && dto.getStatus() != 1) {
            throw new BadRequestException("Problem set status must be 0 or 1");
        }
    }

    private boolean isVisible(ProblemSet problemSet, Long userId) {
        return problemSet.getStatus() == 1
                || (userId != null && problemSet.getCreatorId() != null && problemSet.getCreatorId().equals(userId));
    }

    private Map<Long, UserVO> loadUsersByIds(List<Long> userIds) {
        List<Long> ids = userIds == null ? List.of() : userIds.stream()
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        try {
            ResponseResult<List<UserVO>> result = authUserFeignClient.listUsersByIds(ids);
            if (result == null || result.getCode() != 200 || result.getData() == null) {
                return Map.of();
            }
            return result.getData().stream()
                    .filter(user -> user.getId() != null)
                    .collect(Collectors.toMap(UserVO::getId, Function.identity(), (left, right) -> left));
        } catch (Exception e) {
            log.warn("load users failed, ids={}", ids, e);
            return Map.of();
        }
    }

    private String nicknameOf(Long userId, Map<Long, UserVO> usersById) {
        UserVO user = usersById.get(userId);
        return user != null && StringUtils.hasText(user.getNickname()) ? user.getNickname() : "";
    }

    private ProblemSetVO toVO(ProblemSet problemSet, boolean withProblems, Long userId, Map<Long, UserVO> usersById) {
        ProblemSetVO vo = new ProblemSetVO();
        BeanUtils.copyProperties(problemSet, vo);
        vo.setCreatorNickname(nicknameOf(problemSet.getCreatorId(), usersById));
        vo.setProblemCount(Math.toIntExact(problemSetProblemMapper.selectCount(
                new LambdaQueryWrapper<ProblemSetProblem>().eq(ProblemSetProblem::getSetId, problemSet.getId()))));
        if (withProblems) {
            vo.setProblems(selectProblemVOs(problemSet.getId(), userId));
        }
        return vo;
    }

    private List<ProblemSetProblemVO> selectProblemVOs(Long setId, Long userId) {
        List<ProblemSetProblem> associations = problemSetProblemMapper.selectList(
                new LambdaQueryWrapper<ProblemSetProblem>()
                        .eq(ProblemSetProblem::getSetId, setId)
                        .orderByAsc(ProblemSetProblem::getSortOrder)
                        .orderByAsc(ProblemSetProblem::getId));
        if (associations.isEmpty()) {
            return List.of();
        }

        List<Long> problemIds = associations.stream().map(ProblemSetProblem::getProblemId).toList();
        Map<Long, Problem> problemMap = problemMapper.selectBatchIds(problemIds).stream()
                .collect(Collectors.toMap(Problem::getId, Function.identity()));
        return associations.stream().map(association -> {
            ProblemSetProblemVO vo = new ProblemSetProblemVO();
            BeanUtils.copyProperties(association, vo);
            Problem problem = problemMap.get(association.getProblemId());
            if (problem != null && (problem.getStatus() == 1 || userId != null)) {
                ProblemVO problemVO = problemService.getProblemDetail(problem.getId());
                vo.setProblem(problemVO);
            }
            return vo;
        }).toList();
    }
}
