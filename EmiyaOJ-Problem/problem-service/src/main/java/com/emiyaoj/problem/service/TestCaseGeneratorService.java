package com.emiyaoj.problem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.common.exception.BadRequestException;
import com.emiyaoj.common.exception.BaseException;
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
import com.emiyaoj.problem.dto.TestCaseGeneratorUpdateDTO;
import com.emiyaoj.problem.dto.TestCaseGeneratorVO;
import com.emiyaoj.problem.dto.TestCaseSaveDTO;
import com.emiyaoj.problem.dto.TestCaseVO;
import com.emiyaoj.problem.mapper.ProblemMapper;
import com.emiyaoj.problem.mapper.TestCaseGeneratorMapper;
import com.emiyaoj.problem.mapper.TestCaseMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestCaseGeneratorService extends ServiceImpl<TestCaseGeneratorMapper, TestCaseGenerator> {

    private static final String SAVE_MODE_APPEND = "APPEND";
    private static final String SAVE_MODE_REPLACE = "REPLACE";

    private final ProblemMapper problemMapper;
    private final TestCaseMapper testCaseMapper;
    private final TestCaseGeneratorJudgeFeignClient generatorJudgeFeignClient;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;

    @Value("${judge.internal-token:emiyaoj-judge-internal}")
    private String judgeInternalToken;

    @Value("${test-case-generator.max-generated-cases:1000}")
    private int maxGeneratedCases;

    public TestCaseGeneratorSpecVO createTestCaseGeneratorSpec(Long problemId, TestCaseGeneratorSpecSaveDTO dto) {
        requireProblem(problemId);
        validateSpec(dto);
        if (selectByProblemId(problemId) != null) {
            throw new BadRequestException("Test case generator already exists");
        }

        LocalDateTime now = LocalDateTime.now();
        TestCaseGenerator generator = new TestCaseGenerator();
        generator.setProblemId(problemId);
        generator.setSpec(dto.getSpec());
        generator.setDeleted(0);
        generator.setCreateBy(0L);
        generator.setUpdateBy(0L);
        generator.setCreateTime(now);
        generator.setUpdateTime(now);
        this.save(generator);
        return toSpecVO(generator);
    }

    public TestCaseGeneratorSpecVO updateTestCaseGeneratorSpec(Long problemId, TestCaseGeneratorSpecSaveDTO dto) {
        requireProblem(problemId);
        validateSpec(dto);
        TestCaseGenerator generator = requireGenerator(problemId);
        generator.setSpec(dto.getSpec());
        generator.setUpdateBy(0L);
        generator.setUpdateTime(LocalDateTime.now());
        this.updateById(generator);
        return toSpecVO(generator);
    }

    public TestCaseGeneratorSpecVO getTestCaseGeneratorSpec(Long problemId) {
        requireProblem(problemId);
        TestCaseGenerator generator = selectByProblemId(problemId);
        return generator == null ? null : toSpecVO(generator);
    }

    public TestCaseGeneratorVO getTestCaseGenerator(Long problemId) {
        requireProblem(problemId);
        TestCaseGenerator generator = selectByProblemId(problemId);
        return generator == null ? null : toVO(generator);
    }

    public TestCaseGeneratorVO updateTestCaseGenerator(Long problemId, TestCaseGeneratorUpdateDTO dto) {
        requireProblem(problemId);
        validateGeneratorCode(dto);
        TestCaseGenerator generator = requireGenerator(problemId);
        generator.setGeneratorCode(dto.getGeneratorCode());
        generator.setUpdateBy(0L);
        generator.setUpdateTime(LocalDateTime.now());
        this.updateById(generator);
        return toVO(generator);
    }

    public RunTestCaseGeneratorVO runTestCaseGenerator(Long problemId, RunTestCaseGeneratorDTO dto) {
        requireProblem(problemId);
        String saveMode = normalizeSaveMode(dto);
        TestCaseGenerator generator = requireGenerator(problemId);
        if (!StringUtils.hasText(generator.getGeneratorCode())) {
            throw new BadRequestException("Test case generator code cannot be empty");
        }

        TestCaseGeneratorRunResultVO runResult = callJudgeRunner(generator.getGeneratorCode());
        if (!Boolean.TRUE.equals(runResult.getSuccess())) {
            String message = StringUtils.hasText(runResult.getErrorMessage())
                    ? runResult.getErrorMessage()
                    : "Test case generator failed";
            throw new BaseException(400, message);
        }

        List<TestCaseSaveDTO> generatedCases = parseGeneratedCases(runResult.getStdout());
        List<TestCaseVO> savedCases = new TransactionTemplate(transactionManager)
                .execute(status -> persistGeneratedCases(problemId, generatedCases, saveMode));

        RunTestCaseGeneratorVO vo = new RunTestCaseGeneratorVO();
        vo.setProblemId(problemId);
        vo.setSaveMode(saveMode);
        vo.setGeneratedCount(generatedCases.size());
        vo.setSavedCount(savedCases == null ? 0 : savedCases.size());
        vo.setTimeUsed(runResult.getTimeUsed());
        vo.setMemoryUsed(runResult.getMemoryUsed());
        vo.setTestCases(savedCases);
        return vo;
    }

    private TestCaseGeneratorRunResultVO callJudgeRunner(String generatorCode) {
        TestCaseGeneratorRunRequestDTO request = new TestCaseGeneratorRunRequestDTO();
        request.setGeneratorCode(generatorCode);
        ResponseResult<TestCaseGeneratorRunResultVO> result =
                generatorJudgeFeignClient.runGenerator(judgeInternalToken, request);
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            throw new BaseException(500, "Failed to run test case generator");
        }
        return result.getData();
    }

    private List<TestCaseSaveDTO> parseGeneratedCases(String stdout) {
        if (!StringUtils.hasText(stdout)) {
            throw new BadRequestException("Generator stdout cannot be empty");
        }
        try {
            List<TestCaseSaveDTO> cases = objectMapper.readValue(stdout,
                    new TypeReference<List<TestCaseSaveDTO>>() {});
            validateGeneratedCases(cases);
            return cases;
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Generator stdout must be a JSON array of test cases");
        }
    }

    private void validateGeneratedCases(List<TestCaseSaveDTO> cases) {
        if (cases == null || cases.isEmpty()) {
            throw new BadRequestException("Generator must produce at least one test case");
        }
        if (cases.size() > maxGeneratedCases) {
            throw new BadRequestException("Generated test case count exceeds " + maxGeneratedCases);
        }
        for (int i = 0; i < cases.size(); i++) {
            TestCaseSaveDTO item = cases.get(i);
            if (item == null) {
                throw new BadRequestException("Generated test case cannot be null");
            }
            if (item.getIsSample() != null && item.getIsSample() != 0 && item.getIsSample() != 1) {
                throw new BadRequestException("isSample must be 0 or 1 at index " + i);
            }
            if (item.getScore() != null && item.getScore() < 0) {
                throw new BadRequestException("score cannot be negative at index " + i);
            }
        }
    }

    private List<TestCaseVO> persistGeneratedCases(Long problemId, List<TestCaseSaveDTO> generatedCases,
                                                   String saveMode) {
        if (SAVE_MODE_REPLACE.equals(saveMode)) {
            testCaseMapper.delete(new LambdaQueryWrapper<TestCase>().eq(TestCase::getProblemId, problemId));
        }

        AtomicInteger nextSortOrder = new AtomicInteger(selectNextSortOrder(problemId));
        return generatedCases.stream()
                .map(dto -> saveGeneratedCase(problemId, dto, nextSortOrder))
                .toList();
    }

    private TestCaseVO saveGeneratedCase(Long problemId, TestCaseSaveDTO dto, AtomicInteger nextSortOrder) {
        TestCase testCase = new TestCase();
        testCase.setProblemId(problemId);
        testCase.setInput(dto.getInput());
        testCase.setOutput(dto.getOutput());
        testCase.setIsSample(dto.getIsSample() == null ? 0 : dto.getIsSample());
        testCase.setScore(dto.getScore() == null ? 0 : dto.getScore());
        testCase.setSortOrder(dto.getSortOrder() == null ? nextSortOrder.getAndIncrement() : dto.getSortOrder());
        testCase.setDeleted(0);
        testCase.setCreateTime(LocalDateTime.now());
        testCase.setUpdateTime(LocalDateTime.now());
        testCaseMapper.insert(testCase);

        TestCaseVO vo = new TestCaseVO();
        BeanUtils.copyProperties(testCase, vo);
        return vo;
    }

    private int selectNextSortOrder(Long problemId) {
        List<TestCase> existing = testCaseMapper.selectList(new LambdaQueryWrapper<TestCase>()
                .eq(TestCase::getProblemId, problemId)
                .orderByDesc(TestCase::getSortOrder)
                .last("LIMIT 1"));
        if (existing.isEmpty() || existing.get(0).getSortOrder() == null) {
            return 1;
        }
        return existing.get(0).getSortOrder() + 1;
    }

    private String normalizeSaveMode(RunTestCaseGeneratorDTO dto) {
        String saveMode = dto == null || !StringUtils.hasText(dto.getSaveMode())
                ? SAVE_MODE_APPEND
                : dto.getSaveMode().trim().toUpperCase(Locale.ROOT);
        if (!SAVE_MODE_APPEND.equals(saveMode) && !SAVE_MODE_REPLACE.equals(saveMode)) {
            throw new BadRequestException("saveMode must be APPEND or REPLACE");
        }
        return saveMode;
    }

    private void validateSpec(TestCaseGeneratorSpecSaveDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getSpec())) {
            throw new BadRequestException("Test case generator spec cannot be empty");
        }
    }

    private void validateGeneratorCode(TestCaseGeneratorUpdateDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getGeneratorCode())) {
            throw new BadRequestException("Test case generator code cannot be empty");
        }
    }

    private Problem requireProblem(Long problemId) {
        if (problemId == null) {
            throw new BadRequestException("Problem id cannot be empty");
        }
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new BaseException(404, "Problem does not exist");
        }
        return problem;
    }

    private TestCaseGenerator requireGenerator(Long problemId) {
        TestCaseGenerator generator = selectByProblemId(problemId);
        if (generator == null) {
            throw new BaseException(404, "Test case generator does not exist");
        }
        return generator;
    }

    private TestCaseGenerator selectByProblemId(Long problemId) {
        return this.getBaseMapper().selectOne(new LambdaQueryWrapper<TestCaseGenerator>()
                .eq(TestCaseGenerator::getProblemId, problemId)
                .last("LIMIT 1"), false);
    }

    private TestCaseGeneratorSpecVO toSpecVO(TestCaseGenerator generator) {
        TestCaseGeneratorSpecVO vo = new TestCaseGeneratorSpecVO();
        BeanUtils.copyProperties(generator, vo);
        return vo;
    }

    private TestCaseGeneratorVO toVO(TestCaseGenerator generator) {
        TestCaseGeneratorVO vo = new TestCaseGeneratorVO();
        BeanUtils.copyProperties(generator, vo);
        return vo;
    }
}
