package com.emiyaoj.judge.config;

import com.emiyaoj.judge.dto.AgentRabbitConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentRabbitConfig {

    @Bean
    public TopicExchange agentExchange() {
        return new TopicExchange(AgentRabbitConstants.EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange agentDeadLetterExchange() {
        return new TopicExchange(AgentRabbitConstants.DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue judgeFeedbackQueue() {
        return QueueBuilder.durable(AgentRabbitConstants.JUDGE_FEEDBACK_QUEUE)
                .deadLetterExchange(AgentRabbitConstants.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(AgentRabbitConstants.JUDGE_FEEDBACK_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue judgeFeedbackDeadLetterQueue() {
        return QueueBuilder.durable(AgentRabbitConstants.JUDGE_FEEDBACK_DLQ).build();
    }

    @Bean
    public Binding judgeFeedbackBinding(Queue judgeFeedbackQueue, TopicExchange agentExchange) {
        return BindingBuilder.bind(judgeFeedbackQueue)
                .to(agentExchange)
                .with(AgentRabbitConstants.JUDGE_FEEDBACK_ROUTING_KEY);
    }

    @Bean
    public Binding judgeFeedbackDeadLetterBinding(Queue judgeFeedbackDeadLetterQueue,
                                                  TopicExchange agentDeadLetterExchange) {
        return BindingBuilder.bind(judgeFeedbackDeadLetterQueue)
                .to(agentDeadLetterExchange)
                .with(AgentRabbitConstants.JUDGE_FEEDBACK_DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
