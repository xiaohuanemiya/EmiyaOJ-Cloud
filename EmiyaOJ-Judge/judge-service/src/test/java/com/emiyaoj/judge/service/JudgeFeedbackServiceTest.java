package com.emiyaoj.judge.service;

import com.emiyaoj.judge.domain.JudgeStatus;
import com.emiyaoj.judge.domain.entity.JudgeFeedback;
import com.emiyaoj.judge.domain.entity.Submission;
import com.emiyaoj.judge.domain.entity.SubmissionJudgeResult;
import com.emiyaoj.judge.dto.AgentRabbitConstants;
import com.emiyaoj.judge.dto.JudgeFeedbackVO;
import com.emiyaoj.judge.mapper.JudgeFeedbackMapper;
import com.emiyaoj.judge.mapper.SubmissionCaseResultMapper;
import com.emiyaoj.judge.mapper.SubmissionJudgeResultMapper;
import com.emiyaoj.judge.mapper.SubmissionMapper;
import com.emiyaoj.problem.api.ProblemFeignClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JudgeFeedbackServiceTest {

    private final JudgeFeedbackMapper feedbackMapper = mock(JudgeFeedbackMapper.class);
    private final SubmissionMapper submissionMapper = mock(SubmissionMapper.class);
    private final SubmissionJudgeResultMapper judgeResultMapper = mock(SubmissionJudgeResultMapper.class);
    private final SubmissionCaseResultMapper caseResultMapper = mock(SubmissionCaseResultMapper.class);
    private final ProblemFeignClient problemFeignClient = mock(ProblemFeignClient.class);
    private final AgentTaskPublisher taskPublisher = mock(AgentTaskPublisher.class);
    private final JudgeFeedbackService service = new JudgeFeedbackService(
            feedbackMapper,
            submissionMapper,
            judgeResultMapper,
            caseResultMapper,
            problemFeignClient,
            taskPublisher
    );

    @Test
    void requestFeedbackPublishesOnlyWhenPendingRowIsFirstInserted() {
        Submission submission = submission();
        SubmissionJudgeResult result = judgeResult(JudgeStatus.WRONG_ANSWER);
        when(feedbackMapper.insertPendingIfAbsent(
                submission.getId(), AgentRabbitConstants.AGENT_TYPE_JUDGE_FEEDBACK))
                .thenReturn(1, 0);
        when(taskPublisher.publishJudgeFeedbackTask(submission, result)).thenReturn(true);

        service.requestFeedback(submission, result);
        service.requestFeedback(submission, result);

        verify(taskPublisher).publishJudgeFeedbackTask(submission, result);
    }

    @Test
    void acceptedSubmissionDoesNotCreateOrPublishFeedback() {
        Submission submission = submission();
        SubmissionJudgeResult result = judgeResult(JudgeStatus.ACCEPTED);

        service.requestFeedback(submission, result);

        verify(feedbackMapper, never()).insertPendingIfAbsent(
                submission.getId(), AgentRabbitConstants.AGENT_TYPE_JUDGE_FEEDBACK);
        verify(taskPublisher, never()).publishJudgeFeedbackTask(submission, result);
    }

    @Test
    void onlySuccessfulNonBlankAgentOutputIsVisible() {
        JudgeFeedback pending = feedback("PENDING", null);
        JudgeFeedback noOutput = feedback("NO_OUTPUT", null);
        JudgeFeedback oldFallback = feedback("STATIC_FALLBACK", "旧的静态兜底内容");
        JudgeFeedback blankSuccess = feedback("SUCCESS", " ");
        JudgeFeedback success = feedback("SUCCESS", "结合失败样例检查循环边界。");
        when(feedbackMapper.selectOne(any()))
                .thenReturn(pending, noOutput, oldFallback, blankSuccess, success);

        assertNull(service.getFeedbackBySubmissionId(100L));
        assertNull(service.getFeedbackBySubmissionId(100L));
        assertNull(service.getFeedbackBySubmissionId(100L));
        assertNull(service.getFeedbackBySubmissionId(100L));
        JudgeFeedbackVO visible = service.getFeedbackBySubmissionId(100L);

        assertEquals("结合失败样例检查循环边界。", visible.getContent());
    }

    private Submission submission() {
        Submission submission = new Submission();
        submission.setId(100L);
        submission.setProblemId(200L);
        submission.setUserId(300L);
        return submission;
    }

    private SubmissionJudgeResult judgeResult(int status) {
        SubmissionJudgeResult result = new SubmissionJudgeResult();
        result.setSubmissionId(100L);
        result.setStatus(status);
        return result;
    }

    private JudgeFeedback feedback(String status, String content) {
        JudgeFeedback feedback = new JudgeFeedback();
        feedback.setSubmissionId(100L);
        feedback.setStatus(status);
        feedback.setContent(content);
        return feedback;
    }
}
