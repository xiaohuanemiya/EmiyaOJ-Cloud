package com.emiyaoj.blog.service;

import com.emiyaoj.moderation.dto.ModerationRabbitConstants;
import com.emiyaoj.moderation.dto.ModerationTaskMessage;
import com.emiyaoj.moderation.dto.ModerationTargetType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ModerationTaskPublisher {

    private final RabbitTemplate rabbitTemplate;

    public boolean publishBlog(ModerationTaskMessage message) {
        return publish(message, ModerationRabbitConstants.BLOG_TEXT_ROUTING_KEY);
    }

    public boolean publishComment(ModerationTaskMessage message) {
        return publish(message, ModerationRabbitConstants.COMMENT_TEXT_ROUTING_KEY);
    }

    private boolean publish(ModerationTaskMessage message, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(ModerationRabbitConstants.EXCHANGE, routingKey, message);
            log.info("Moderation message sent, taskId={}, targetType={}, targetId={}",
                    message.getTaskId(), message.getTargetType(), message.getTargetId());
            return true;
        } catch (Exception e) {
            ModerationTargetType targetType = message == null ? null : message.getTargetType();
            Long targetId = message == null ? null : message.getTargetId();
            log.error("Send moderation message failed, routingKey={}, targetType={}, targetId={}",
                    routingKey, targetType, targetId, e);
            return false;
        }
    }
}
