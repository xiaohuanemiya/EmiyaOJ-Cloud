package com.emiyaoj.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.exception.BaseException;
import com.emiyaoj.problem.domain.pojo.Problem;
import com.emiyaoj.problem.domain.pojo.ProblemPicture;
import com.emiyaoj.problem.domain.pojo.ProblemTag;
import com.emiyaoj.problem.domain.pojo.Tag;
import com.emiyaoj.problem.dto.ProblemPictureVO;
import com.emiyaoj.problem.dto.ProblemQueryDTO;
import com.emiyaoj.problem.dto.ProblemSaveDTO;
import com.emiyaoj.problem.dto.ProblemVO;
import com.emiyaoj.problem.mapper.ProblemMapper;
import com.emiyaoj.problem.mapper.ProblemPictureMapper;
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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Problem service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemService extends ServiceImpl<ProblemMapper, Problem> {

    private final ProblemTagMapper problemTagMapper;
    private final TagMapper tagMapper;
    private final ProblemPictureMapper problemPictureMapper;
    private final ProblemImageUrlResolver problemImageUrlResolver;

    public PageVO<ProblemVO> queryProblemPage(ProblemQueryDTO queryDTO) {
        Page<Problem> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getTitle()), Problem::getTitle, queryDTO.getTitle())
               .eq(queryDTO.getDifficulty() != null, Problem::getDifficulty, queryDTO.getDifficulty())
               .eq(queryDTO.getStatus() != null, Problem::getStatus, queryDTO.getStatus())
               .orderByDesc(Problem::getCreateTime);

        this.page(page, wrapper);

        return PageVO.of(page, problem -> convertToVO(problem, false));
    }

    public ProblemVO getProblemDetail(Long id) {
        Problem problem = this.getById(id);
        if (problem == null) {
            return null;
        }
        return convertToVO(problem, true);
    }

    public List<ProblemVO> listPublicProblemsByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        List<Long> problemIds = ids.stream()
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (problemIds.isEmpty()) {
            return List.of();
        }
        return this.list(new LambdaQueryWrapper<Problem>()
                        .in(Problem::getId, problemIds)
                        .eq(Problem::getStatus, 1)
                        .eq(Problem::getDeleted, 0))
                .stream()
                .map(problem -> convertToVO(problem, false))
                .toList();
    }

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

        if (saved && !CollectionUtils.isEmpty(dto.getTagIds())) {
            saveTagAssociations(problem.getId(), dto.getTagIds());
        }
        if (saved && dto.getPictureIds() != null) {
            bindPictures(problem.getId(), dto.getPictureIds(), operatorId);
        }
        return saved;
    }

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

        if (updated && dto.getTagIds() != null) {
            problemTagMapper.delete(new LambdaQueryWrapper<ProblemTag>()
                    .eq(ProblemTag::getProblemId, dto.getId()));
            if (!dto.getTagIds().isEmpty()) {
                saveTagAssociations(dto.getId(), dto.getTagIds());
            }
        }
        if (updated && dto.getPictureIds() != null) {
            bindPictures(dto.getId(), dto.getPictureIds(), operatorId);
        }
        return updated;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProblem(Long id) {
        boolean removed = this.removeById(id);
        if (removed) {
            problemPictureMapper.update(new UpdateWrapper<ProblemPicture>()
                    .eq("problem_id", id)
                    .set("problem_id", null));
        }
        return removed;
    }

    private void saveTagAssociations(Long problemId, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            ProblemTag pt = new ProblemTag();
            pt.setProblemId(problemId);
            pt.setTagId(tagId);
            pt.setCreateTime(LocalDateTime.now());
            problemTagMapper.insert(pt);
        }
    }

    private void bindPictures(Long problemId, List<Long> pictureIds, Long userId) {
        problemPictureMapper.update(new UpdateWrapper<ProblemPicture>()
                .eq("problem_id", problemId)
                .set("problem_id", null));
        if (CollectionUtils.isEmpty(pictureIds)) {
            return;
        }
        if (pictureIds.stream().anyMatch(Objects::isNull)) {
            throw new BaseException(400, "图片不存在");
        }
        List<Long> ids = pictureIds.stream().distinct().toList();
        List<ProblemPicture> pictures = problemPictureMapper.selectByIds(ids);
        if (pictures.size() != ids.size()) {
            throw new BaseException(400, "图片不存在");
        }
        boolean invalid = pictures.stream().anyMatch(picture ->
                !picture.getUserId().equals(userId) || Integer.valueOf(1).equals(picture.getDeleted()));
        if (invalid) {
            throw new BaseException(400, "只能绑定自己上传的有效图片");
        }
        problemPictureMapper.update(new UpdateWrapper<ProblemPicture>()
                .in("id", ids)
                .set("problem_id", problemId));
    }

    private ProblemVO convertToVO(Problem problem, boolean includePictures) {
        ProblemVO vo = new ProblemVO();
        BeanUtils.copyProperties(problem, vo);
        vo.setDescription(problemImageUrlResolver.rewriteLegacyContentUrls(problem.getDescription()));
        vo.setInputDescription(problemImageUrlResolver.rewriteLegacyContentUrls(problem.getInputDescription()));
        vo.setOutputDescription(problemImageUrlResolver.rewriteLegacyContentUrls(problem.getOutputDescription()));
        vo.setHint(problemImageUrlResolver.rewriteLegacyContentUrls(problem.getHint()));
        vo.setDifficultyDesc(difficultyDesc(problem.getDifficulty()));
        vo.setTags(selectProblemTags(problem.getId()));
        vo.setPictures(includePictures ? selectProblemPictures(problem.getId()) : Collections.emptyList());
        return vo;
    }

    private String difficultyDesc(Integer difficulty) {
        return switch (difficulty == null ? 0 : difficulty) {
            case 1 -> "简单";
            case 2 -> "中等";
            case 3 -> "困难";
            default -> "未知";
        };
    }

    private List<String> selectProblemTags(Long problemId) {
        List<ProblemTag> problemTags = problemTagMapper.selectList(
                new LambdaQueryWrapper<ProblemTag>().eq(ProblemTag::getProblemId, problemId));
        if (CollectionUtils.isEmpty(problemTags)) {
            return Collections.emptyList();
        }
        List<Long> tagIds = problemTags.stream().map(ProblemTag::getTagId).toList();
        List<Tag> tags = tagMapper.selectBatchIds(tagIds);
        return tags.stream().map(Tag::getName).collect(Collectors.toList());
    }

    private List<ProblemPictureVO> selectProblemPictures(Long problemId) {
        return problemPictureMapper.selectList(new LambdaQueryWrapper<ProblemPicture>()
                        .eq(ProblemPicture::getProblemId, problemId)
                        .eq(ProblemPicture::getDeleted, 0)
                        .orderByAsc(ProblemPicture::getCreateTime))
                .stream()
                .map(this::convertPictureToVO)
                .toList();
    }

    private ProblemPictureVO convertPictureToVO(ProblemPicture picture) {
        ProblemPictureVO vo = new ProblemPictureVO();
        BeanUtils.copyProperties(picture, vo);
        vo.setUrl(problemImageUrlResolver.buildPublicUrl(picture.getObjectName()));
        return vo;
    }
}
