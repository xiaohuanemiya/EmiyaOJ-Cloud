package com.emiyaoj.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.emiyaoj.common.exception.BaseException;
import com.emiyaoj.problem.domain.pojo.Problem;
import com.emiyaoj.problem.domain.pojo.ProblemPicture;
import com.emiyaoj.problem.dto.ProblemSaveDTO;
import com.emiyaoj.problem.mapper.ProblemMapper;
import com.emiyaoj.problem.mapper.ProblemPictureMapper;
import com.emiyaoj.problem.mapper.ProblemTagMapper;
import com.emiyaoj.problem.mapper.TagMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProblemServiceImageBindingTest {

    @Mock
    private ProblemMapper problemMapper;

    @Mock
    private ProblemTagMapper problemTagMapper;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private ProblemPictureMapper problemPictureMapper;

    @Mock
    private ProblemImageUrlResolver problemImageUrlResolver;

    @InjectMocks
    private ProblemService problemService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(problemService, "baseMapper", problemMapper);
    }

    @Test
    void saveProblemBindsUploadedPictures() {
        when(problemMapper.insert(any(Problem.class))).thenAnswer(invocation -> {
            Problem problem = invocation.getArgument(0);
            problem.setId(10L);
            return 1;
        });
        when(problemPictureMapper.selectByIds(List.of(1L, 2L)))
                .thenReturn(List.of(picture(1L, 100L, 0), picture(2L, 100L, 0)));
        when(problemPictureMapper.update(any(UpdateWrapper.class))).thenReturn(1);

        ProblemSaveDTO dto = validProblem();
        dto.setPictureIds(List.of(1L, 2L));

        assertTrue(problemService.saveProblem(dto, 100L));

        verify(problemPictureMapper, times(2)).update(any(UpdateWrapper.class));
    }

    @Test
    void updateProblemKeepsPicturesWhenPictureIdsIsNull() {
        when(problemMapper.selectById(10L)).thenReturn(existingProblem());
        when(problemMapper.updateById(any(Problem.class))).thenReturn(1);

        ProblemSaveDTO dto = validProblem();
        dto.setId(10L);
        dto.setPictureIds(null);

        assertTrue(problemService.updateProblem(dto, 100L));

        verify(problemPictureMapper, never()).update(any(UpdateWrapper.class));
        verify(problemPictureMapper, never()).selectByIds(any());
    }

    @Test
    void updateProblemClearsPicturesWhenPictureIdsIsEmpty() {
        when(problemMapper.selectById(10L)).thenReturn(existingProblem());
        when(problemMapper.updateById(any(Problem.class))).thenReturn(1);
        when(problemPictureMapper.update(any(UpdateWrapper.class))).thenReturn(1);

        ProblemSaveDTO dto = validProblem();
        dto.setId(10L);
        dto.setPictureIds(List.of());

        assertTrue(problemService.updateProblem(dto, 100L));

        verify(problemPictureMapper, times(1)).update(any(UpdateWrapper.class));
        verify(problemPictureMapper, never()).selectByIds(any());
    }

    @Test
    void saveProblemRejectsMissingPictureId() {
        when(problemMapper.insert(any(Problem.class))).thenAnswer(invocation -> {
            Problem problem = invocation.getArgument(0);
            problem.setId(10L);
            return 1;
        });
        when(problemPictureMapper.update(any(UpdateWrapper.class))).thenReturn(1);
        when(problemPictureMapper.selectByIds(List.of(99L))).thenReturn(List.of());

        ProblemSaveDTO dto = validProblem();
        dto.setPictureIds(List.of(99L));

        BaseException ex = assertThrows(BaseException.class, () -> problemService.saveProblem(dto, 100L));

        assertEquals(400, ex.getCode());
    }

    @Test
    void saveProblemRejectsPictureUploadedByAnotherUser() {
        when(problemMapper.insert(any(Problem.class))).thenAnswer(invocation -> {
            Problem problem = invocation.getArgument(0);
            problem.setId(10L);
            return 1;
        });
        when(problemPictureMapper.update(any(UpdateWrapper.class))).thenReturn(1);
        when(problemPictureMapper.selectByIds(List.of(1L))).thenReturn(List.of(picture(1L, 200L, 0)));

        ProblemSaveDTO dto = validProblem();
        dto.setPictureIds(List.of(1L));

        BaseException ex = assertThrows(BaseException.class, () -> problemService.saveProblem(dto, 100L));

        assertEquals(400, ex.getCode());
    }

    @Test
    void deleteProblemUnbindsPictures() {
        when(problemMapper.deleteById(10L)).thenReturn(1);
        when(problemPictureMapper.update(any(UpdateWrapper.class))).thenReturn(1);

        assertTrue(problemService.deleteProblem(10L));

        verify(problemPictureMapper).update(any(UpdateWrapper.class));
    }

    @Test
    void detailReturnsBoundPicturesOnlyForDetailView() {
        Problem problem = existingProblem();
        problem.setDescription("![x](/problem-images/problem/a.png)");
        when(problemMapper.selectById(10L)).thenReturn(problem);
        when(problemImageUrlResolver.rewriteLegacyContentUrls(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(problemTagMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(problemPictureMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(picture(1L, 100L, 0)));
        when(problemImageUrlResolver.buildPublicUrl(any())).thenReturn("http://127.0.0.1:9000/problem-images/problem/a.png");

        assertEquals(1, problemService.getProblemDetail(10L).getPictures().size());
    }

    private ProblemSaveDTO validProblem() {
        ProblemSaveDTO dto = new ProblemSaveDTO();
        dto.setTitle("A+B");
        dto.setDescription("desc");
        dto.setDifficulty(1);
        dto.setTimeLimit(1000);
        dto.setMemoryLimit(256);
        dto.setStatus(1);
        return dto;
    }

    private Problem existingProblem() {
        Problem problem = new Problem();
        problem.setId(10L);
        problem.setTitle("A+B");
        problem.setDescription("desc");
        problem.setDifficulty(1);
        problem.setTimeLimit(1000);
        problem.setMemoryLimit(256);
        problem.setStatus(1);
        problem.setDeleted(0);
        return problem;
    }

    private ProblemPicture picture(Long id, Long userId, Integer deleted) {
        ProblemPicture picture = new ProblemPicture();
        picture.setId(id);
        picture.setUserId(userId);
        picture.setObjectName("problem/%d/a.png".formatted(userId));
        picture.setContentType("image/png");
        picture.setSize(3L);
        picture.setOriginalFilename("a.png");
        picture.setDeleted(deleted);
        return picture;
    }
}
