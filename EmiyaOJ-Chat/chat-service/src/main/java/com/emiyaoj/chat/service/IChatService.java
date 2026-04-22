package com.emiyaoj.chat.service;

import com.emiyaoj.chat.dto.ChatRequestDTO;

/**
 * AI 聊天服务接口
 */
public interface IChatService {

    /**
     * 发送消息并获取 AI 回复
     */
    String sendMessage(ChatRequestDTO requestDTO);
}
