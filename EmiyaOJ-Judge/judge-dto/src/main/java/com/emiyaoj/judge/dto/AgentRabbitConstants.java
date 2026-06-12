package com.emiyaoj.judge.dto;

/**
 * Common RabbitMQ names for the extensible Agent platform.
 */
public final class AgentRabbitConstants {

    private AgentRabbitConstants() {
    }

    public static final String EXCHANGE = "emiyaoj.agent.exchange";
    public static final String DEAD_LETTER_EXCHANGE = "emiyaoj.agent.dlx";

    public static final String JUDGE_FEEDBACK_QUEUE = "emiyaoj.agent.judge-feedback.queue";
    public static final String JUDGE_FEEDBACK_DLQ = "emiyaoj.agent.judge-feedback.dlq";

    public static final String JUDGE_FEEDBACK_ROUTING_KEY = "agent.judge.feedback";
    public static final String JUDGE_FEEDBACK_DLQ_ROUTING_KEY = "agent.judge.feedback.dead";

    public static final String AGENT_TYPE_JUDGE_FEEDBACK = "JUDGE_FEEDBACK";
}
