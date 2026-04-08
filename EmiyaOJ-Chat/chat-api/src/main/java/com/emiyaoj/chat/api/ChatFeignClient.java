package com.emiyaoj.chat.api;

import com.emiyaoj.chat.dto.ChatRequestDTO;
import com.emiyaoj.common.domain.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * AI 聊天服务 Feign 调用客户端
 */
@FeignClient(value = "chat-service", contextId = "chatFeignClient")
public interface ChatFeignClient {

    @PostMapping("/client/chat/send")
    ResponseResult<String> sendMessage(@RequestBody ChatRequestDTO requestDTO);
}
