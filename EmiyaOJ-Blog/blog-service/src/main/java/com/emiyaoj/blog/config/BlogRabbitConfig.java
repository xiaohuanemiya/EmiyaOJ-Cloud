package com.emiyaoj.blog.config;

import com.emiyaoj.moderation.dto.ModerationRabbitConstants;
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
public class BlogRabbitConfig {

    @Bean
    public TopicExchange moderationExchange() {
        return new TopicExchange(ModerationRabbitConstants.EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange moderationDeadLetterExchange() {
        return new TopicExchange(ModerationRabbitConstants.DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue blogTextQueue() {
        return QueueBuilder.durable(ModerationRabbitConstants.BLOG_TEXT_QUEUE)
                .deadLetterExchange(ModerationRabbitConstants.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(ModerationRabbitConstants.BLOG_TEXT_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue commentTextQueue() {
        return QueueBuilder.durable(ModerationRabbitConstants.COMMENT_TEXT_QUEUE)
                .deadLetterExchange(ModerationRabbitConstants.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(ModerationRabbitConstants.COMMENT_TEXT_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue blogTextDeadLetterQueue() {
        return QueueBuilder.durable(ModerationRabbitConstants.BLOG_TEXT_DLQ).build();
    }

    @Bean
    public Queue commentTextDeadLetterQueue() {
        return QueueBuilder.durable(ModerationRabbitConstants.COMMENT_TEXT_DLQ).build();
    }

    @Bean
    public Binding blogTextBinding(Queue blogTextQueue, TopicExchange moderationExchange) {
        return BindingBuilder.bind(blogTextQueue)
                .to(moderationExchange)
                .with(ModerationRabbitConstants.BLOG_TEXT_ROUTING_KEY);
    }

    @Bean
    public Binding commentTextBinding(Queue commentTextQueue, TopicExchange moderationExchange) {
        return BindingBuilder.bind(commentTextQueue)
                .to(moderationExchange)
                .with(ModerationRabbitConstants.COMMENT_TEXT_ROUTING_KEY);
    }

    @Bean
    public Binding blogTextDeadLetterBinding(Queue blogTextDeadLetterQueue, TopicExchange moderationDeadLetterExchange) {
        return BindingBuilder.bind(blogTextDeadLetterQueue)
                .to(moderationDeadLetterExchange)
                .with(ModerationRabbitConstants.BLOG_TEXT_DLQ_ROUTING_KEY);
    }

    @Bean
    public Binding commentTextDeadLetterBinding(Queue commentTextDeadLetterQueue, TopicExchange moderationDeadLetterExchange) {
        return BindingBuilder.bind(commentTextDeadLetterQueue)
                .to(moderationDeadLetterExchange)
                .with(ModerationRabbitConstants.COMMENT_TEXT_DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
