package com.emiyaoj.judge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.emiyaoj.judge.domain.entity.MessageEvent;
import com.emiyaoj.judge.mapper.MessageEventMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地消息表定时任务
 * 定期扫描待处理的消息, 执行判题任务, 失败自动重试
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageEventScheduler {

    private final MessageEventMapper messageEventMapper;
    private final SubmissionService submissionService;
    private final ObjectMapper objectMapper;

    /**
     * 每5秒扫描一次待处理消息
     */
    @Scheduled(fixedDelay = 5000)
    public void processMessageEvents() {
        // 查询待处理 & 待重试的消息
        LambdaQueryWrapper<MessageEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(MessageEvent::getStatus, 0, 3) // 待处理 或 处理失败(需重试)
                .le(MessageEvent::getNextRetryTime, LocalDateTime.now())
                .lt(MessageEvent::getRetryCount, 3) // retryCount < maxRetryCount
                .orderByAsc(MessageEvent::getCreateTime)
                .last("LIMIT 10"); // 每次最多处理10条

        List<MessageEvent> events = messageEventMapper.selectList(wrapper);

        for (MessageEvent event : events) {
            processEvent(event);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void processEvent(MessageEvent event) {
        try {
            // 更新为处理中
            event.setStatus(1);
            event.setRetryCount(event.getRetryCount() + 1);
            event.setUpdateTime(LocalDateTime.now());
            messageEventMapper.updateById(event);

            // 解析消息内容
            JsonNode payload = objectMapper.readTree(event.getPayload());
            Long submissionId = payload.get("submissionId").asLong();
            Long problemId = payload.get("problemId").asLong();
            Long languageId = payload.get("languageId").asLong();
            String code = payload.get("code").asText();

            // 执行判题
            submissionService.executeJudge(submissionId, problemId, languageId, code);

            // 标记为成功
            event.setStatus(2);
            event.setUpdateTime(LocalDateTime.now());
            messageEventMapper.updateById(event);

            log.info("Message event processed successfully: id={}, submissionId={}", event.getId(), submissionId);
        } catch (Exception e) {
            log.error("Message event processing failed: id={}", event.getId(), e);

            // 标记为失败, 设置下次重试时间 (指数退避)
            event.setStatus(3);
            int delay = (int) Math.pow(2, event.getRetryCount()) * 10; // 20s, 40s, 80s...
            event.setNextRetryTime(LocalDateTime.now().plusSeconds(delay));
            event.setUpdateTime(LocalDateTime.now());
            messageEventMapper.updateById(event);
        }
    }
}
