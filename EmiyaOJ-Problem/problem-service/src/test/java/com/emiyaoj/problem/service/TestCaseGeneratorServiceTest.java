package com.emiyaoj.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.common.exception.BadRequestException;
import com.emiyaoj.judge.api.TestCaseGeneratorJudgeFeignClient;
import com.emiyaoj.judge.dto.TestCaseGeneratorRunRequestDTO;
import com.emiyaoj.judge.dto.TestCaseGeneratorRunResultVO;
import com.emiyaoj.problem.domain.pojo.Problem;
import com.emiyaoj.problem.domain.pojo.TestCase;
import com.emiyaoj.problem.domain.pojo.TestCaseGenerator;
import com.emiyaoj.problem.dto.RunTestCaseGeneratorDTO;
import com.emiyaoj.problem.dto.RunTestCaseGeneratorVO;
import com.emiyaoj.problem.dto.TestCaseGeneratorSpecSaveDTO;
import com.emiyaoj.problem.dto.TestCaseGeneratorSpecVO;
import com.emiyaoj.problem.mapper.ProblemMapper;
import com.emiyaoj.problem.mapper.TestCaseGeneratorMapper;
import com.emiyaoj.problem.mapper.TestCaseMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TestCaseGeneratorServiceTest {

    private TestCaseGeneratorMapper generatorMapper;
    private ProblemMapper problemMapper;
    private TestCaseMapper testCaseMapper;
    private TestCaseGeneratorJudgeFeignClient judgeFeignClient;
    private TestCaseGeneratorService service;

    @BeforeEach
    void setUp() {
        generatorMapper = mock(TestCaseGeneratorMapper.class);
        problemMapper = mock(ProblemMapper.class);
        testCaseMapper = mock(TestCaseMapper.class);
        judgeFeignClient = mock(TestCaseGeneratorJudgeFeignClient.class);
        service = new TestCaseGeneratorService(
                problemMapper,
                testCaseMapper,
                judgeFeignClient,
                new ObjectMapper(),
                transactionManager()
        );
        ReflectionTestUtils.setField(service, "baseMapper", generatorMapper);
        ReflectionTestUtils.setField(service, "judgeInternalToken", "internal-token");
        ReflectionTestUtils.setField(service, "maxGeneratedCases", 1000);
    }

    @Test
    void createSpecSavesGenerator_whenProblemExistsAndPermissionAllowed() {
        when(problemMapper.selectById(1L)).thenReturn(new Problem());
        when(generatorMapper.selectOne(any(LambdaQueryWrapper.class), any(Boolean.class))).thenReturn(null);
        when(generatorMapper.insert(any(TestCaseGenerator.class))).thenReturn(1);

        TestCaseGeneratorSpecSaveDTO dto = new TestCaseGeneratorSpecSaveDTO();
        dto.setSpec("Generate A+B cases");

        TestCaseGeneratorSpecVO vo = service.createTestCaseGeneratorSpec(1L, dto);

        assertEquals(1L, vo.getProblemId());
        assertEquals("Generate A+B cases", vo.getSpec());
        verify(generatorMapper).insert(any(TestCaseGenerator.class));
    }

    @Test
    void createSpecRejectsDuplicateGenerator() {
        when(problemMapper.selectById(1L)).thenReturn(new Problem());
        when(generatorMapper.selectOne(any(LambdaQueryWrapper.class), any(Boolean.class)))
                .thenReturn(existingGenerator());

        TestCaseGeneratorSpecSaveDTO dto = new TestCaseGeneratorSpecSaveDTO();
        dto.setSpec("Spec");

        assertThrows(BadRequestException.class,
                () -> service.createTestCaseGeneratorSpec(1L, dto));
    }

    @Test
    void runGeneratorAppendsGeneratedCasesAndAssignsSortOrders() {
        when(problemMapper.selectById(1L)).thenReturn(new Problem());
        when(generatorMapper.selectOne(any(LambdaQueryWrapper.class), any(Boolean.class)))
                .thenReturn(existingGenerator());
        when(judgeFeignClient.runGenerator(eq("internal-token"), any(TestCaseGeneratorRunRequestDTO.class)))
                .thenReturn(ResponseResult.success(successRun("""
                        [{"input":"1 2\\n","output":"3\\n"},{"input":"4 5\\n","output":"9\\n","sortOrder":20}]
                        """)));
        TestCase existing = new TestCase();
        existing.setSortOrder(7);
        when(testCaseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(existing));
        when(testCaseMapper.insert(any(TestCase.class))).thenReturn(1);

        RunTestCaseGeneratorVO vo = service.runTestCaseGenerator(1L, new RunTestCaseGeneratorDTO());

        assertEquals("APPEND", vo.getSaveMode());
        assertEquals(2, vo.getSavedCount());
        ArgumentCaptor<TestCase> captor = ArgumentCaptor.forClass(TestCase.class);
        verify(testCaseMapper, org.mockito.Mockito.times(2)).insert(captor.capture());
        assertEquals(8, captor.getAllValues().get(0).getSortOrder());
        assertEquals(20, captor.getAllValues().get(1).getSortOrder());
    }

    @Test
    void runGeneratorReplaceDeletesExistingCasesBeforeSaving() {
        when(problemMapper.selectById(1L)).thenReturn(new Problem());
        when(generatorMapper.selectOne(any(LambdaQueryWrapper.class), any(Boolean.class)))
                .thenReturn(existingGenerator());
        when(judgeFeignClient.runGenerator(eq("internal-token"), any(TestCaseGeneratorRunRequestDTO.class)))
                .thenReturn(ResponseResult.success(successRun("[{\"input\":\"\",\"output\":\"ok\\n\"}]")));
        when(testCaseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(testCaseMapper.insert(any(TestCase.class))).thenReturn(1);
        RunTestCaseGeneratorDTO dto = new RunTestCaseGeneratorDTO();
        dto.setSaveMode("REPLACE");

        RunTestCaseGeneratorVO vo = service.runTestCaseGenerator(1L, dto);

        assertEquals("REPLACE", vo.getSaveMode());
        verify(testCaseMapper).delete(any(LambdaQueryWrapper.class));
        verify(testCaseMapper).insert(any(TestCase.class));
    }

    @Test
    void runGeneratorAllowsMissingAndNullInputOutput() {
        when(problemMapper.selectById(1L)).thenReturn(new Problem());
        when(generatorMapper.selectOne(any(LambdaQueryWrapper.class), any(Boolean.class)))
                .thenReturn(existingGenerator());
        when(judgeFeignClient.runGenerator(eq("internal-token"), any(TestCaseGeneratorRunRequestDTO.class)))
                .thenReturn(ResponseResult.success(successRun("""
                        [{"isSample":0,"score":1},{"input":null,"output":null,"sortOrder":5}]
                        """)));
        when(testCaseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(testCaseMapper.insert(any(TestCase.class))).thenReturn(1);

        RunTestCaseGeneratorVO vo = service.runTestCaseGenerator(1L, new RunTestCaseGeneratorDTO());

        assertEquals(2, vo.getSavedCount());
        ArgumentCaptor<TestCase> captor = ArgumentCaptor.forClass(TestCase.class);
        verify(testCaseMapper, org.mockito.Mockito.times(2)).insert(captor.capture());
        assertNull(captor.getAllValues().get(0).getInput());
        assertNull(captor.getAllValues().get(0).getOutput());
        assertEquals(1, captor.getAllValues().get(0).getSortOrder());
        assertNull(captor.getAllValues().get(1).getInput());
        assertNull(captor.getAllValues().get(1).getOutput());
        assertEquals(5, captor.getAllValues().get(1).getSortOrder());
    }

    @Test
    void runGeneratorDoesNotPersistInvalidJson() {
        when(problemMapper.selectById(1L)).thenReturn(new Problem());
        when(generatorMapper.selectOne(any(LambdaQueryWrapper.class), any(Boolean.class)))
                .thenReturn(existingGenerator());
        when(judgeFeignClient.runGenerator(eq("internal-token"), any(TestCaseGeneratorRunRequestDTO.class)))
                .thenReturn(ResponseResult.success(successRun("not-json")));

        assertThrows(BadRequestException.class,
                () -> service.runTestCaseGenerator(1L, new RunTestCaseGeneratorDTO()));
        verify(testCaseMapper, never()).insert(any(TestCase.class));
    }

    private TestCaseGenerator existingGenerator() {
        TestCaseGenerator generator = new TestCaseGenerator();
        generator.setId(100L);
        generator.setProblemId(1L);
        generator.setSpec("Spec");
        generator.setGeneratorCode("print('[]')");
        return generator;
    }

    private TestCaseGeneratorRunResultVO successRun(String stdout) {
        TestCaseGeneratorRunResultVO result = new TestCaseGeneratorRunResultVO();
        result.setSuccess(true);
        result.setStatus("Accepted");
        result.setStdout(stdout);
        result.setTimeUsed(10L);
        result.setMemoryUsed(1024L);
        return result;
    }

    private PlatformTransactionManager transactionManager() {
        return new AbstractPlatformTransactionManager() {
            @Override
            protected Object doGetTransaction() {
                return new Object();
            }

            @Override
            protected void doBegin(Object transaction, TransactionDefinition definition) {
            }

            @Override
            protected void doCommit(DefaultTransactionStatus status) {
            }

            @Override
            protected void doRollback(DefaultTransactionStatus status) {
            }
        };
    }
}
