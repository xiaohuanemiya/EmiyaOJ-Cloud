package com.emiyaoj.chat.controller;

import com.emiyaoj.chat.dto.ChatRequestDTO;
import com.emiyaoj.chat.service.IChatService;
import com.emiyaoj.common.domain.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AI 聊天接口（面向前端用户）
 */
@Tag(name = "AI 聊天")
@RestController
@RequestMapping("/client/chat")
@RequiredArgsConstructor
public class ChatController {

    private final IChatService chatService;

    /**
     * 发送消息给 AI 助手
     */
    @PostMapping("/send")
    public ResponseResult<String> sendMessage(@RequestBody ChatRequestDTO requestDTO) {
        String reply = chatService.sendMessage(requestDTO);
        return ResponseResult.success(reply);
    }
}
