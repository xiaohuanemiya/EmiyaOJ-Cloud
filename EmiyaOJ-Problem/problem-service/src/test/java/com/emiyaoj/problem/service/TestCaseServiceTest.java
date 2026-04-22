package com.emiyaoj.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.emiyaoj.problem.domain.pojo.TestCase;
import com.emiyaoj.problem.dto.TestCaseSaveDTO;
import com.emiyaoj.problem.dto.TestCaseVO;
import com.emiyaoj.problem.mapper.TestCaseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 测试用例服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class TestCaseServiceTest {

    @Mock
    private TestCaseMapper testCaseMapper;

    @InjectMocks
    private TestCaseService testCaseService;

    @BeforeEach
    void setUp() {
        // 确保 baseMapper 正确注入（ServiceImpl 通过 Spring 注入 baseMapper 字段）
        ReflectionTestUtils.setField(testCaseService, "baseMapper", testCaseMapper);
    }

    // ======================== getByProblemId ========================

    @Test
    void getByProblemId_shouldReturnMappedVOList() {
        TestCase tc1 = buildTestCase(1L, 10L, "1 2", "3", 1, 10, 0);
        TestCase tc2 = buildTestCase(2L, 10L, "3 4", "7", 0, 10, 1);
        when(testCaseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(tc1, tc2));

        List<TestCaseVO> result = testCaseService.getByProblemId(10L);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("1 2", result.get(0).getInput());
        assertEquals("3", result.get(0).getOutput());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void getByProblemId_shouldReturnEmptyList_whenNone() {
        when(testCaseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        List<TestCaseVO> result = testCaseService.getByProblemId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ======================== getTestCaseById ========================

    @Test
    void getTestCaseById_shouldReturnVO_whenExists() {
        TestCase tc = buildTestCase(1L, 10L, "input", "output", 1, 10, 0);
        when(testCaseMapper.selectById(1L)).thenReturn(tc);

        TestCaseVO vo = testCaseService.getTestCaseById(1L);

        assertNotNull(vo);
        assertEquals(1L, vo.getId());
        assertEquals("input", vo.getInput());
        assertEquals("output", vo.getOutput());
    }

    @Test
    void getTestCaseById_shouldReturnNull_whenNotExists() {
        when(testCaseMapper.selectById(99L)).thenReturn(null);

        TestCaseVO vo = testCaseService.getTestCaseById(99L);

        assertNull(vo);
    }

    // ======================== saveTestCase ========================

    @Test
    void saveTestCase_shouldSaveAndReturnVO() {
        when(testCaseMapper.insert(any(TestCase.class))).thenReturn(1);

        TestCaseSaveDTO dto = new TestCaseSaveDTO();
        dto.setProblemId(10L);
        dto.setInput("a b");
        dto.setOutput("ab");
        dto.setIsSample(1);
        dto.setScore(20);
        dto.setSortOrder(0);

        TestCaseVO vo = testCaseService.saveTestCase(dto);

        assertNotNull(vo);
        assertEquals(10L, vo.getProblemId());
        assertEquals("a b", vo.getInput());
        assertEquals("ab", vo.getOutput());
        assertEquals(1, vo.getIsSample());
        verify(testCaseMapper, times(1)).insert(any(TestCase.class));
    }

    // ======================== batchSaveTestCases ========================

    @Test
    void batchSaveTestCases_shouldSaveAllAndReturnVOList() {
        when(testCaseMapper.insert(any(TestCase.class))).thenReturn(1);

        TestCaseSaveDTO dto1 = new TestCaseSaveDTO();
        dto1.setInput("1");
        dto1.setOutput("1");

        TestCaseSaveDTO dto2 = new TestCaseSaveDTO();
        dto2.setInput("2");
        dto2.setOutput("4");

        List<TestCaseVO> result = testCaseService.batchSaveTestCases(10L, Arrays.asList(dto1, dto2));

        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).getProblemId());
        assertEquals(10L, result.get(1).getProblemId());
        verify(testCaseMapper, times(2)).insert(any(TestCase.class));
    }

    // ======================== updateTestCase ========================

    @Test
    void updateTestCase_shouldUpdateSuccessfully_whenExists() {
        TestCase existing = buildTestCase(1L, 10L, "old input", "old output", 0, 5, 0);
        when(testCaseMapper.selectById(1L)).thenReturn(existing);
        when(testCaseMapper.updateById(any(TestCase.class))).thenReturn(1);

        TestCaseSaveDTO dto = new TestCaseSaveDTO();
        dto.setId(1L);
        dto.setProblemId(10L);
        dto.setInput("new input");
        dto.setOutput("new output");
        dto.setScore(10);

        boolean result = testCaseService.updateTestCase(dto);

        assertTrue(result);
        verify(testCaseMapper, times(1)).updateById(any(TestCase.class));
    }

    @Test
    void updateTestCase_shouldThrow_whenNotExists() {
        when(testCaseMapper.selectById(99L)).thenReturn(null);

        TestCaseSaveDTO dto = new TestCaseSaveDTO();
        dto.setId(99L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> testCaseService.updateTestCase(dto));
        assertEquals("测试用例不存在", ex.getMessage());
    }

    // ======================== deleteTestCaseById ========================

    @Test
    void deleteTestCaseById_shouldReturnTrue_onSuccess() {
        when(testCaseMapper.deleteById(1L)).thenReturn(1);

        boolean result = testCaseService.deleteTestCaseById(1L);

        assertTrue(result);
        verify(testCaseMapper, times(1)).deleteById(1L);
    }

    // ======================== batchDeleteByIds ========================

    @Test
    void batchDeleteByIds_shouldReturnFalse_whenListEmpty() {
        boolean result = testCaseService.batchDeleteByIds(List.of());

        assertFalse(result);
        verifyNoInteractions(testCaseMapper);
    }

    @Test
    void batchDeleteByIds_shouldDeleteAll_whenListNotEmpty() {
        when(testCaseMapper.deleteBatchIds(any())).thenReturn(3);

        boolean result = testCaseService.batchDeleteByIds(Arrays.asList(1L, 2L, 3L));

        assertTrue(result);
        verify(testCaseMapper, times(1)).deleteBatchIds(any());
    }

    // ======================== deleteByProblemId ========================

    @Test
    void deleteByProblemId_shouldDeleteAllForProblem() {
        when(testCaseMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(5);

        boolean result = testCaseService.deleteByProblemId(10L);

        assertTrue(result);
        verify(testCaseMapper, times(1)).delete(any(LambdaQueryWrapper.class));
    }

    // ======================== helper ========================

    private TestCase buildTestCase(Long id, Long problemId, String input, String output,
                                    Integer isSample, Integer score, Integer sortOrder) {
        TestCase tc = new TestCase();
        tc.setId(id);
        tc.setProblemId(problemId);
        tc.setInput(input);
        tc.setOutput(output);
        tc.setIsSample(isSample);
        tc.setScore(score);
        tc.setSortOrder(sortOrder);
        tc.setDeleted(0);
        tc.setCreateTime(LocalDateTime.now());
        tc.setUpdateTime(LocalDateTime.now());
        return tc;
    }
}
