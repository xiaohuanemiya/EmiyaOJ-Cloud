package com.emiyaoj.judge.service;

import com.emiyaoj.judge.domain.JudgeStatus;
import com.emiyaoj.judge.domain.entity.Submission;
import com.emiyaoj.judge.domain.entity.SubmissionJudgeResult;
import com.emiyaoj.judge.dto.AgentRabbitConstants;
import com.emiyaoj.judge.dto.AgentTaskMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentTaskPublisher {

    private final RabbitTemplate rabbitTemplate;

    public boolean publishJudgeFeedbackTask(Submission submission, SubmissionJudgeResult judgeResult) {
        if (!shouldPublish(submission, judgeResult)) {
            return true;
        }

        String traceId = UUID.randomUUID().toString();
        AgentTaskMessage message = AgentTaskMessage.builder()
                .agentType(AgentRabbitConstants.AGENT_TYPE_JUDGE_FEEDBACK)
                .taskId(UUID.randomUUID().toString())
                .traceId(traceId)
                .submissionId(submission.getId())
                .problemId(submission.getProblemId())
                .userId(submission.getUserId())
                .status(judgeResult.getStatus())
                .createdAt(LocalDateTime.now().toString())
                .build();

        try {
            rabbitTemplate.convertAndSend(
                    AgentRabbitConstants.EXCHANGE,
                    AgentRabbitConstants.JUDGE_FEEDBACK_ROUTING_KEY,
                    message
            );
            log.info("Agent task sent, agentType={}, submissionId={}, traceId={}",
                    message.getAgentType(), message.getSubmissionId(), traceId);
            return true;
        } catch (Exception e) {
            log.error("Send Agent task failed, submissionId={}, status={}",
                    submission.getId(), judgeResult.getStatus(), e);
            return false;
        }
    }

    private boolean shouldPublish(Submission submission, SubmissionJudgeResult judgeResult) {
        return submission != null
                && submission.getId() != null
                && judgeResult != null
                && judgeResult.getStatus() != null
                && judgeResult.getStatus() != JudgeStatus.ACCEPTED
                && judgeResult.getStatus() != JudgeStatus.PENDING
                && judgeResult.getStatus() != JudgeStatus.JUDGING;
    }
}
