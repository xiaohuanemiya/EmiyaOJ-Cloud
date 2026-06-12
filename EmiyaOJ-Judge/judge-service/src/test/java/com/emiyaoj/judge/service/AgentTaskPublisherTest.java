package com.emiyaoj.judge.service;

import com.emiyaoj.judge.domain.JudgeStatus;
import com.emiyaoj.judge.domain.entity.Submission;
import com.emiyaoj.judge.domain.entity.SubmissionJudgeResult;
import com.emiyaoj.judge.dto.AgentRabbitConstants;
import com.emiyaoj.judge.dto.AgentTaskMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AgentTaskPublisherTest {

    @Test
    void acceptedSubmissionDoesNotPublishFeedbackTask() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        AgentTaskPublisher publisher = new AgentTaskPublisher(rabbitTemplate);

        boolean published = publisher.publishJudgeFeedbackTask(submission(), judgeResult(JudgeStatus.ACCEPTED));

        assertTrue(published);
        verify(rabbitTemplate, never()).convertAndSend(
                eq(AgentRabbitConstants.EXCHANGE),
                eq(AgentRabbitConstants.JUDGE_FEEDBACK_ROUTING_KEY),
                org.mockito.ArgumentMatchers.any(Object.class)
        );
    }

    @Test
    void nonAcceptedSubmissionPublishesJudgeFeedbackTask() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        AgentTaskPublisher publisher = new AgentTaskPublisher(rabbitTemplate);
        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);

        boolean published = publisher.publishJudgeFeedbackTask(submission(), judgeResult(JudgeStatus.WRONG_ANSWER));

        assertTrue(published);
        verify(rabbitTemplate).convertAndSend(
                eq(AgentRabbitConstants.EXCHANGE),
                eq(AgentRabbitConstants.JUDGE_FEEDBACK_ROUTING_KEY),
                messageCaptor.capture()
        );
        AgentTaskMessage message = (AgentTaskMessage) messageCaptor.getValue();
        assertEquals(AgentRabbitConstants.AGENT_TYPE_JUDGE_FEEDBACK, message.getAgentType());
        assertEquals(100L, message.getSubmissionId());
        assertEquals(200L, message.getProblemId());
        assertEquals(300L, message.getUserId());
        assertEquals(JudgeStatus.WRONG_ANSWER, message.getStatus());
        assertNotNull(message.getTaskId());
        assertNotNull(message.getTraceId());
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
}
