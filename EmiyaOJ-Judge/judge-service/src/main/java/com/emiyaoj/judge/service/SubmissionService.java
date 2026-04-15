package com.emiyaoj.judge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.emiyaoj.common.domain.PageDTO;
import com.emiyaoj.common.domain.PageVO;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.common.exception.BaseException;
import com.emiyaoj.judge.domain.entity.MessageEvent;
import com.emiyaoj.judge.domain.entity.Submission;
import com.emiyaoj.judge.domain.gojudge.GoJudgeResult;
import com.emiyaoj.judge.domain.gojudge.GoJudgeStatus;
import com.emiyaoj.judge.dto.SubmissionVO;
import com.emiyaoj.judge.dto.SubmitCodeDTO;
import com.emiyaoj.judge.mapper.MessageEventMapper;
import com.emiyaoj.judge.mapper.SubmissionMapper;
import com.emiyaoj.problem.api.ProblemFeignClient;
import com.emiyaoj.problem.dto.LanguageVO;
import com.emiyaoj.problem.dto.ProblemVO;
import com.emiyaoj.problem.dto.TestCaseVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 提交 & 判题服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionMapper submissionMapper;
    private final MessageEventMapper messageEventMapper;
    private final GoJudgeService goJudgeService;
    private final ProblemFeignClient problemFeignClient;
    private final ObjectMapper objectMapper;

    /**
     * 提交代码 - 使用本地消息表保证分布式事务可靠性
     * 1. 在同一个本地事务中: 创建 submission 记录 + 插入 message_event
     * 2. 异步/定时任务处理 message_event, 调用 GoJudge 判题
     */
    @Transactional(rollbackFor = Exception.class)
    public SubmissionVO submitCode(SubmitCodeDTO dto, Long userId) {
        // 1. 远程调用获取题目信息
        ResponseResult<ProblemVO> problemResult = problemFeignClient.getProblemById(dto.getProblemId());
        if (problemResult == null || problemResult.getCode() != 200 || problemResult.getData() == null) {
            throw new BaseException(400, "题目不存在");
        }

        // 2. 远程调用获取语言信息
        ResponseResult<LanguageVO> languageResult = problemFeignClient.getLanguageById(dto.getLanguageId());
        if (languageResult == null || languageResult.getCode() != 200 || languageResult.getData() == null) {
            throw new BaseException(400, "编程语言不存在");
        }

        // 3. 创建提交记录
        Submission submission = new Submission();
        submission.setProblemId(dto.getProblemId());
        submission.setUserId(userId);
        submission.setLanguageId(dto.getLanguageId());
        submission.setCode(dto.getCode());
        submission.setStatus(0); // 待判题
        submission.setScore(0);
        submission.setCreateTime(LocalDateTime.now());
        submission.setUpdateTime(LocalDateTime.now());
        submission.setDeleted(0);
        submissionMapper.insert(submission);

        // 4. 插入本地消息表 (同一事务)
        MessageEvent event = new MessageEvent();
        event.setBusinessType("JUDGE_SUBMIT");
        event.setBusinessId(submission.getId());
        event.setStatus(0); // 待处理
        event.setRetryCount(0);
        event.setMaxRetryCount(3);
        try {
            event.setPayload(objectMapper.writeValueAsString(Map.of(
                    "submissionId", submission.getId(),
                    "problemId", dto.getProblemId(),
                    "languageId", dto.getLanguageId(),
                    "code", dto.getCode()
            )));
        } catch (Exception e) {
            event.setPayload("{}");
        }
        event.setNextRetryTime(LocalDateTime.now());
        event.setCreateTime(LocalDateTime.now());
        event.setUpdateTime(LocalDateTime.now());
        messageEventMapper.insert(event);

        log.info("Submission created: id={}, messageEventId={}", submission.getId(), event.getId());

        // 5. 返回提交记录
        SubmissionVO vo = new SubmissionVO();
        BeanUtils.copyProperties(submission, vo);
        return vo;
    }

    /**
     * 执行判题 (由定时任务或直接调用)
     */
    public void executeJudge(Long submissionId, Long problemId, Long languageId, String code) {
        // 更新状态为判题中
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            log.error("Submission not found: {}", submissionId);
            return;
        }
        submission.setStatus(1); // 判题中
        submission.setUpdateTime(LocalDateTime.now());
        submissionMapper.updateById(submission);

        try {
            // 获取语言信息
            ResponseResult<LanguageVO> langResult = problemFeignClient.getLanguageById(languageId);
            if (langResult == null || langResult.getData() == null) {
                updateSubmissionError(submission, "获取语言信息失败");
                return;
            }
            String languageName = langResult.getData().getName();

            // 获取测试用例
            ResponseResult<List<TestCaseVO>> testCaseResult = problemFeignClient.getTestCasesByProblemId(problemId);
            if (testCaseResult == null || testCaseResult.getData() == null || testCaseResult.getData().isEmpty()) {
                updateSubmissionError(submission, "获取测试用例失败或无测试用例");
                return;
            }
            List<TestCaseVO> testCases = testCaseResult.getData();

            // 获取题目信息 (获取时间/内存限制)
            ResponseResult<ProblemVO> problemResult = problemFeignClient.getProblemById(problemId);
            ProblemVO problem = problemResult != null ? problemResult.getData() : null;
            long timeLimit = problem != null && problem.getTimeLimit() != null ? problem.getTimeLimit() : 1000;
            long memoryLimit = problem != null && problem.getMemoryLimit() != null ? problem.getMemoryLimit() : 262144; // 256MB in KB

            // 编译
            Map<String, String> fileIds = null;
            if (!languageName.toLowerCase().contains("python")) {
                GoJudgeResult compileResult = goJudgeService.compile(code, languageName);
                if (compileResult != null) {
                    if (!GoJudgeStatus.ACCEPTED.getValue().equals(compileResult.getStatus())) {
                        String compileError = "";
                        if (compileResult.getFiles() != null) {
                            compileError = compileResult.getFiles().getOrDefault("stderr", "");
                        }
                        if (compileError.isEmpty() && compileResult.getError() != null) {
                            compileError = compileResult.getError();
                        }
                        updateSubmissionCompileError(submission, compileError);
                        return;
                    }
                    fileIds = compileResult.getFileIds();
                }
            }

            // 逐个运行测试用例
            int passCount = 0;
            long maxTime = 0;
            long maxMemory = 0;
            String lastError = null;

            for (TestCaseVO testCase : testCases) {
                GoJudgeResult runResult = goJudgeService.run(languageName, fileIds, code, testCase, timeLimit, memoryLimit);

                if (runResult == null) {
                    lastError = "运行无返回结果";
                    continue;
                }

                // 记录资源使用
                if (runResult.getTime() != null) {
                    maxTime = Math.max(maxTime, runResult.getTime() / 1_000_000); // 纳秒 -> 毫秒
                }
                if (runResult.getMemory() != null) {
                    maxMemory = Math.max(maxMemory, runResult.getMemory() / 1024); // 字节 -> KB
                }

                if (!GoJudgeStatus.ACCEPTED.getValue().equals(runResult.getStatus())) {
                    GoJudgeStatus status = GoJudgeStatus.fromValue(runResult.getStatus());
                    lastError = switch (status) {
                        case TIME_LIMIT_EXCEEDED -> "时间超限";
                        case MEMORY_LIMIT_EXCEEDED -> "内存超限";
                        case NONZERO_EXIT_STATUS -> "运行时错误";
                        case OUTPUT_LIMIT_EXCEEDED -> "输出超限";
                        default -> runResult.getStatus();
                    };
                    continue;
                }

                // 比较输出
                String actualOutput = runResult.getFiles() != null
                        ? runResult.getFiles().getOrDefault("stdout", "").trim()
                        : "";
                String expectedOutput = testCase.getOutput() != null
                        ? testCase.getOutput().trim()
                        : "";

                if (actualOutput.equals(expectedOutput)) {
                    passCount++;
                }
            }

            // 清理缓存文件
            if (fileIds != null) {
                fileIds.values().forEach(goJudgeService::deleteFile);
            }

            // 计算结果
            double passRate = testCases.isEmpty() ? 0 : (double) passCount / testCases.size();
            int score = (int) Math.round(passRate * 100);
            int status = 2; // 都算已完成

            submission.setStatus(status);
            submission.setScore(score);
            submission.setTimeUsed(maxTime);
            submission.setMemoryUsed(maxMemory);
            submission.setPassRate(passRate);
            if (lastError != null && passCount < testCases.size()) {
                submission.setErrorMessage(lastError);
            }
            submission.setUpdateTime(LocalDateTime.now());
            submissionMapper.updateById(submission);

            log.info("Judge complete: submissionId={}, score={}, passRate={}", submissionId, score, passRate);

        } catch (Exception e) {
            log.error("Judge failed for submission: {}", submissionId, e);
            updateSubmissionError(submission, "系统错误: " + e.getMessage());
        }
    }

    /**
     * 查询提交记录详情
     */
    public SubmissionVO getSubmissionById(Long id) {
        Submission submission = submissionMapper.selectById(id);
        if (submission == null) {
            return null;
        }
        SubmissionVO vo = new SubmissionVO();
        BeanUtils.copyProperties(submission, vo);
        return vo;
    }

    /**
     * 分页查询提交记录
     */
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

        List<SubmissionVO> voList = page.getRecords().stream().map(s -> {
            SubmissionVO vo = new SubmissionVO();
            BeanUtils.copyProperties(s, vo);
            return vo;
        }).toList();

        return new PageVO<>(page.getTotal(), voList, (long) pageDTO.getPageNum(), (long) pageDTO.getPageSize());
    }

    // ==================== 私有方法 ====================

    private void updateSubmissionError(Submission submission, String errorMessage) {
        submission.setStatus(4);
        submission.setErrorMessage(errorMessage);
        submission.setScore(0);
        submission.setPassRate(0.0);
        submission.setUpdateTime(LocalDateTime.now());
        submissionMapper.updateById(submission);
    }

    private void updateSubmissionCompileError(Submission submission, String compileMessage) {
        submission.setStatus(3); // 编译错误
        submission.setCompileMessage(compileMessage);
        submission.setScore(0);
        submission.setPassRate(0.0);
        submission.setUpdateTime(LocalDateTime.now());
        submissionMapper.updateById(submission);
    }
}
