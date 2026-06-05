package com.emiyaoj.judge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.common.exception.BaseException;
import com.emiyaoj.judge.domain.JudgeStatus;
import com.emiyaoj.judge.domain.entity.JudgeFeedback;
import com.emiyaoj.judge.domain.entity.Submission;
import com.emiyaoj.judge.domain.entity.SubmissionCaseResult;
import com.emiyaoj.judge.domain.entity.SubmissionJudgeResult;
import com.emiyaoj.judge.dto.AgentRabbitConstants;
import com.emiyaoj.judge.dto.JudgeFeedbackCallbackDTO;
import com.emiyaoj.judge.dto.JudgeFeedbackContextVO;
import com.emiyaoj.judge.dto.JudgeFeedbackVO;
import com.emiyaoj.judge.mapper.JudgeFeedbackMapper;
import com.emiyaoj.judge.mapper.SubmissionCaseResultMapper;
import com.emiyaoj.judge.mapper.SubmissionJudgeResultMapper;
import com.emiyaoj.judge.mapper.SubmissionMapper;
import com.emiyaoj.problem.api.ProblemFeignClient;
import com.emiyaoj.problem.dto.ProblemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudgeFeedbackService {

    private static final int HISTORY_LIMIT = 10;

    private final JudgeFeedbackMapper judgeFeedbackMapper;
    private final SubmissionMapper submissionMapper;
    private final SubmissionJudgeResultMapper judgeResultMapper;
    private final SubmissionCaseResultMapper caseResultMapper;
    private final ProblemFeignClient problemFeignClient;

    public JudgeFeedbackVO getFeedbackBySubmissionId(Long submissionId) {
        JudgeFeedback feedback = selectBySubmissionId(submissionId);
        return toVO(feedback);
    }

    public JudgeFeedbackContextVO buildContext(Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BaseException(404, "Submission does not exist");
        }

        SubmissionJudgeResult judgeResult = selectJudgeResult(submissionId);
        if (judgeResult == null) {
            throw new BaseException(404, "Judge result does not exist");
        }

        return JudgeFeedbackContextVO.builder()
                .submissionId(submission.getId())
                .problemId(submission.getProblemId())
                .userId(submission.getUserId())
                .languageId(submission.getLanguageId())
                .status(judgeResult.getStatus())
                .statusText(JudgeStatus.describe(judgeResult.getStatus()))
                .passedCaseCount(judgeResult.getPassedCaseCount())
                .totalCaseCount(judgeResult.getTotalCaseCount())
                .score(judgeResult.getScore())
                .maxTimeUsed(judgeResult.getMaxTimeUsed())
                .maxMemoryUsed(judgeResult.getMaxMemoryUsed())
                .errorMessage(judgeResult.getErrorMessage())
                .compileMessage(judgeResult.getCompileMessage())
                .code(submission.getCode())
                .finishTime(judgeResult.getFinishTime())
                .problem(loadProblemInfo(submission.getProblemId()))
                .failedCases(loadFailedCaseHints(submissionId))
                .history(loadSubmissionHistory(submission))
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public JudgeFeedbackVO applyCallback(JudgeFeedbackCallbackDTO callback) {
        if (callback == null || callback.getSubmissionId() == null) {
            throw new BaseException(400, "submissionId is required");
        }
        Submission submission = submissionMapper.selectById(callback.getSubmissionId());
        if (submission == null) {
            throw new BaseException(404, "Submission does not exist");
        }

        JudgeFeedback feedback = selectBySubmissionId(callback.getSubmissionId());
        if (feedback == null) {
            feedback = new JudgeFeedback();
            feedback.setSubmissionId(callback.getSubmissionId());
            applyCallbackFields(feedback, callback);
            feedback.setCreateTime(LocalDateTime.now());
            feedback.setUpdateTime(LocalDateTime.now());
            judgeFeedbackMapper.insert(feedback);
            return toVO(feedback);
        }

        applyCallbackFields(feedback, callback);
        feedback.setUpdateTime(LocalDateTime.now());
        judgeFeedbackMapper.updateById(feedback);
        return toVO(feedback);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveStaticFallback(Submission submission, SubmissionJudgeResult judgeResult, String reason) {
        if (submission == null || submission.getId() == null || judgeResult == null) {
            return;
        }
        JudgeFeedbackCallbackDTO callback = JudgeFeedbackCallbackDTO.builder()
                .submissionId(submission.getId())
                .agentType(AgentRabbitConstants.AGENT_TYPE_JUDGE_FEEDBACK)
                .status("STATIC_FALLBACK")
                .source("STATIC_FALLBACK")
                .content(staticFallbackContent(judgeResult))
                .errorMessage(reason)
                .build();
        applyCallback(callback);
    }

    private JudgeFeedbackContextVO.ProblemInfo loadProblemInfo(Long problemId) {
        try {
            ResponseResult<ProblemVO> response = problemFeignClient.getProblemById(problemId);
            ProblemVO problem = response == null ? null : response.getData();
            if (problem == null) {
                return null;
            }
            return JudgeFeedbackContextVO.ProblemInfo.builder()
                    .id(problem.getId())
                    .title(problem.getTitle())
                    .description(problem.getDescription())
                    .inputDescription(problem.getInputDescription())
                    .outputDescription(problem.getOutputDescription())
                    .sampleInput(problem.getSampleInput())
                    .sampleOutput(problem.getSampleOutput())
                    .hint(problem.getHint())
                    .difficultyDesc(problem.getDifficultyDesc())
                    .timeLimit(problem.getTimeLimit())
                    .memoryLimit(problem.getMemoryLimit())
                    .tags(problem.getTags())
                    .build();
        } catch (Exception e) {
            log.warn("Load problem for feedback failed, problemId={}", problemId, e);
            return null;
        }
    }

    private List<JudgeFeedbackContextVO.FailedCaseHint> loadFailedCaseHints(Long submissionId) {
        List<SubmissionCaseResult> caseResults = caseResultMapper.selectList(
                new LambdaQueryWrapper<SubmissionCaseResult>()
                        .eq(SubmissionCaseResult::getSubmissionId, submissionId)
                        .ne(SubmissionCaseResult::getStatus, JudgeStatus.ACCEPTED)
                        .orderByAsc(SubmissionCaseResult::getCaseOrder)
                        .orderByAsc(SubmissionCaseResult::getId)
        );
        return caseResults.stream()
                .map(result -> JudgeFeedbackContextVO.FailedCaseHint.builder()
                        .caseOrder(result.getCaseOrder())
                        .status(result.getStatus())
                        .statusText(JudgeStatus.describe(result.getStatus()))
                        .isSample(result.getIsSample())
                        .score(result.getScore())
                        .timeUsed(result.getTimeUsed())
                        .memoryUsed(result.getMemoryUsed())
                        .errorMessage(result.getErrorMessage())
                        .inputPreview(result.getInputPreview())
                        .expectedOutputPreview(result.getExpectedOutputPreview())
                        .actualOutputPreview(result.getActualOutputPreview())
                        .outputDiffSummary(result.getOutputDiffSummary())
                        .build())
                .toList();
    }

    private JudgeFeedbackContextVO.SubmissionHistory loadSubmissionHistory(Submission submission) {
        List<Submission> submissions = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getUserId, submission.getUserId())
                        .eq(Submission::getProblemId, submission.getProblemId())
                        .orderByDesc(Submission::getCreateTime)
                        .orderByDesc(Submission::getId)
                        .last("LIMIT " + HISTORY_LIMIT)
        );
        if (submissions == null || submissions.isEmpty()) {
            return JudgeFeedbackContextVO.SubmissionHistory.builder()
                    .totalAttempts(0)
                    .nonAcceptedAttempts(0)
                    .acceptedBefore(false)
                    .recentStatuses(List.of())
                    .build();
        }

        List<Long> ids = submissions.stream().map(Submission::getId).toList();
        Map<Long, SubmissionJudgeResult> resultMap = judgeResultMapper.selectList(
                        new LambdaQueryWrapper<SubmissionJudgeResult>()
                                .in(SubmissionJudgeResult::getSubmissionId, ids)
                ).stream()
                .collect(Collectors.toMap(
                        SubmissionJudgeResult::getSubmissionId,
                        Function.identity(),
                        (left, right) -> left
                ));
        List<Integer> statuses = submissions.stream()
                .map(item -> resultMap.get(item.getId()))
                .map(result -> result == null ? null : result.getStatus())
                .toList();
        long acceptedBefore = submissions.stream()
                .filter(item -> !item.getId().equals(submission.getId()))
                .map(item -> resultMap.get(item.getId()))
                .filter(result -> result != null && result.getStatus() != null)
                .filter(result -> result.getStatus() == JudgeStatus.ACCEPTED)
                .count();
        int nonAccepted = Math.toIntExact(statuses.stream()
                .filter(status -> status != null && status != JudgeStatus.ACCEPTED)
                .count());
        return JudgeFeedbackContextVO.SubmissionHistory.builder()
                .totalAttempts(submissions.size())
                .nonAcceptedAttempts(nonAccepted)
                .acceptedBefore(acceptedBefore > 0)
                .recentStatuses(statuses)
                .build();
    }

    private void applyCallbackFields(JudgeFeedback feedback, JudgeFeedbackCallbackDTO callback) {
        feedback.setStatus(defaultString(callback.getStatus(), "SUCCESS"));
        feedback.setContent(callback.getContent());
        feedback.setSource(callback.getSource());
        feedback.setModel(callback.getModel());
        feedback.setAgentType(defaultString(callback.getAgentType(), AgentRabbitConstants.AGENT_TYPE_JUDGE_FEEDBACK));
        feedback.setTraceId(callback.getTraceId());
        feedback.setErrorMessage(callback.getErrorMessage());
    }

    private String staticFallbackContent(SubmissionJudgeResult judgeResult) {
        String statusText = JudgeStatus.describe(judgeResult.getStatus());
        return switch (judgeResult.getStatus()) {
            case JudgeStatus.COMPILE_ERROR -> "编译没有通过。请优先阅读编译信息，检查语法、类名/文件名、头文件或库函数使用是否正确。";
            case JudgeStatus.WRONG_ANSWER, JudgeStatus.PARTIAL_ACCEPTED -> "判题结果为 " + statusText
                    + "。建议先用题目样例和边界数据手动推演，重点检查边界条件、输出格式和数据类型范围。";
            case JudgeStatus.TIME_LIMIT_EXCEEDED -> "程序运行超时。请估算当前算法复杂度，并结合题目数据范围考虑是否需要更高效的数据结构或算法。";
            case JudgeStatus.MEMORY_LIMIT_EXCEEDED -> "程序内存超限。请检查是否开了过大的数组、保存了不必要的中间状态，或存在递归/容器膨胀问题。";
            case JudgeStatus.RUNTIME_ERROR -> "程序运行时错误。请检查数组越界、空引用、除零、递归栈溢出以及输入读取数量是否和题意一致。";
            default -> "当前提交未通过。AI 反馈暂时不可用，请先根据判题状态和错误信息定位问题。";
        };
    }

    private SubmissionJudgeResult selectJudgeResult(Long submissionId) {
        return judgeResultMapper.selectOne(
                new LambdaQueryWrapper<SubmissionJudgeResult>()
                        .eq(SubmissionJudgeResult::getSubmissionId, submissionId)
                        .last("LIMIT 1")
        );
    }

    private JudgeFeedback selectBySubmissionId(Long submissionId) {
        if (submissionId == null) {
            return null;
        }
        return judgeFeedbackMapper.selectOne(
                new LambdaQueryWrapper<JudgeFeedback>()
                        .eq(JudgeFeedback::getSubmissionId, submissionId)
                        .last("LIMIT 1")
        );
    }

    private JudgeFeedbackVO toVO(JudgeFeedback feedback) {
        if (feedback == null) {
            return null;
        }
        JudgeFeedbackVO vo = new JudgeFeedbackVO();
        BeanUtils.copyProperties(feedback, vo);
        return vo;
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
