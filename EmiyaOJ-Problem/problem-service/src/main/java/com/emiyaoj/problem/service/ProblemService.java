package com.emiyaoj.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.problem.domain.pojo.Problem;
import com.emiyaoj.problem.domain.pojo.ProblemTag;
import com.emiyaoj.problem.domain.pojo.Tag;
import com.emiyaoj.problem.dto.ProblemQueryDTO;
import com.emiyaoj.problem.dto.ProblemSaveDTO;
import com.emiyaoj.problem.dto.ProblemVO;
import com.emiyaoj.problem.mapper.ProblemMapper;
import com.emiyaoj.problem.mapper.ProblemTagMapper;
import com.emiyaoj.problem.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemService extends ServiceImpl<ProblemMapper, Problem> {

    private final ProblemTagMapper problemTagMapper;
    private final TagMapper tagMapper;

    /**
     * 分页查询题目列表
     */
    public PageVO<ProblemVO> queryProblemPage(ProblemQueryDTO queryDTO) {
        Page<Problem> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getTitle()), Problem::getTitle, queryDTO.getTitle())
               .eq(queryDTO.getDifficulty() != null, Problem::getDifficulty, queryDTO.getDifficulty())
               .eq(queryDTO.getStatus() != null, Problem::getStatus, queryDTO.getStatus())
               .orderByDesc(Problem::getCreateTime);

        this.page(page, wrapper);

        return PageVO.of(page, this::convertToVO);
    }

    /**
     * 查询题目详情
     */
    public ProblemVO getProblemDetail(Long id) {
        Problem problem = this.getById(id);
        if (problem == null) {
            return null;
        }
        return convertToVO(problem);
    }

    /**
     * 新增题目
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveProblem(ProblemSaveDTO dto, Long operatorId) {
        Problem problem = new Problem();
        BeanUtils.copyProperties(dto, problem);
        problem.setAuthorId(operatorId);
        problem.setCreateBy(operatorId);
        problem.setUpdateBy(operatorId);
        problem.setAcceptCount(0);
        problem.setSubmitCount(0);
        problem.setCreateTime(LocalDateTime.now());
        problem.setUpdateTime(LocalDateTime.now());

        boolean saved = this.save(problem);

        // 保存标签关联
        if (saved && !CollectionUtils.isEmpty(dto.getTagIds())) {
            saveTagAssociations(problem.getId(), dto.getTagIds());
        }
        return saved;
    }

    /**
     * 更新题目
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProblem(ProblemSaveDTO dto, Long operatorId) {
        Problem problem = this.getById(dto.getId());
        if (problem == null) {
            throw new RuntimeException("题目不存在");
        }

        BeanUtils.copyProperties(dto, problem);
        problem.setUpdateBy(operatorId);
        problem.setUpdateTime(LocalDateTime.now());

        boolean updated = this.updateById(problem);

        // 重新关联标签
        if (updated && dto.getTagIds() != null) {
            // 先删除旧关联
            problemTagMapper.delete(new LambdaQueryWrapper<ProblemTag>()
                    .eq(ProblemTag::getProblemId, dto.getId()));
            // 再新增
            if (!dto.getTagIds().isEmpty()) {
                saveTagAssociations(dto.getId(), dto.getTagIds());
            }
        }
        return updated;
    }

    /**
     * 删除题目（逻辑删除）
     */
    public boolean deleteProblem(Long id) {
        return this.removeById(id);
    }

    // ======================== 私有方法 ========================

    private void saveTagAssociations(Long problemId, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            ProblemTag pt = new ProblemTag();
            pt.setProblemId(problemId);
            pt.setTagId(tagId);
            pt.setCreateTime(LocalDateTime.now());
            problemTagMapper.insert(pt);
        }
    }

    private ProblemVO convertToVO(Problem problem) {
        ProblemVO vo = new ProblemVO();
        BeanUtils.copyProperties(problem, vo);

        // 难度描述
        vo.setDifficultyDesc(switch (problem.getDifficulty()) {
            case 1 -> "简单";
            case 2 -> "中等";
            case 3 -> "困难";
            default -> "未知";
        });

        // 查询关联标签
        List<ProblemTag> problemTags = problemTagMapper.selectList(
                new LambdaQueryWrapper<ProblemTag>().eq(ProblemTag::getProblemId, problem.getId()));
        if (!CollectionUtils.isEmpty(problemTags)) {
            List<Long> tagIds = problemTags.stream().map(ProblemTag::getTagId).toList();
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            vo.setTags(tags.stream().map(Tag::getName).collect(Collectors.toList()));
        } else {
            vo.setTags(Collections.emptyList());
        }

        return vo;
    }
}
