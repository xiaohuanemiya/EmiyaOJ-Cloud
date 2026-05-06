package com.emiyaoj.moderation.consumer;

import com.emiyaoj.blog.api.BlogFeignClient;
import com.emiyaoj.common.domain.ResponseResult;
import com.emiyaoj.moderation.config.ModerationProperties;
import com.emiyaoj.moderation.dto.ModerationRabbitConstants;
import com.emiyaoj.moderation.dto.ModerationResultDTO;
import com.emiyaoj.moderation.dto.ModerationTaskMessage;
import com.emiyaoj.moderation.service.TextModerationService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ModerationTaskConsumer {

    private final TextModerationService textModerationService;
    private final BlogFeignClient blogFeignClient;
    private final ModerationProperties properties;

    @RabbitListener(queues = {
            ModerationRabbitConstants.BLOG_TEXT_QUEUE,
            ModerationRabbitConstants.COMMENT_TEXT_QUEUE
    })
    public void consume(ModerationTaskMessage task,
                        Message message,
                        Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            ModerationResultDTO result = textModerationService.moderate(task);
            callbackWithRetry(result);
            channel.basicAck(deliveryTag, false);
            log.info("Moderation task finished, taskId={}, targetType={}, targetId={}, status={}",
                    task.getTaskId(), task.getTargetType(), task.getTargetId(), result.getAuditStatus());
        } catch (Exception e) {
            log.error("Moderation task failed, taskId={}, routingKey={}",
                    task == null ? null : task.getTaskId(),
                    message.getMessageProperties().getReceivedRoutingKey(), e);
            channel.basicReject(deliveryTag, false);
        }
    }

    private void callbackWithRetry(ModerationResultDTO result) {
        int maxAttempts = Math.max(properties.getMaxCallbackRetries(), 1);
        RuntimeException lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                ResponseResult<Void> response = blogFeignClient.applyModerationResult(result);
                if (response != null && response.getCode() == 200) {
                    return;
                }
                String message = response == null ? "empty response" : response.getMessage();
                lastException = new IllegalStateException("Blog callback failed: " + message);
            } catch (RuntimeException e) {
                lastException = e;
            }
            sleepBeforeNextAttempt(attempt, maxAttempts);
        }
        throw lastException == null ? new IllegalStateException("Blog callback failed") : lastException;
    }

    private void sleepBeforeNextAttempt(int attempt, int maxAttempts) {
        if (attempt >= maxAttempts) {
            return;
        }
        try {
            Thread.sleep(Math.max(properties.getCallbackRetryIntervalMillis(), 0L));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Callback retry interrupted", e);
        }
    }
}
