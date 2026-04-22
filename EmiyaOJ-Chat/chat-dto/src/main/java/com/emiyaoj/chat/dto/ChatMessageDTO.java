package com.emiyaoj.chat.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天消息 DTO
 */
@Data
public class ChatMessageDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 角色：user 或 assistant */
    private String role;

    /** 消息内容 */
    private String content;
}
