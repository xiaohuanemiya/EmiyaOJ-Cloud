package com.emiyaoj.chat.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.emiyaoj.chat.config.ChatProperties;
import com.emiyaoj.chat.dto.ChatMessageDTO;
import com.emiyaoj.chat.dto.ChatRequestDTO;
import com.emiyaoj.chat.service.IChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 聊天服务实现（调用阿里云百炼 DashScope API）
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements IChatService {

    private final ChatProperties chatProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            你的角色：一个专业、耐心且循循善诱的 OJ 编程伙伴（c和c++语言）。

            核心使命：启发学生思考，陪伴他们探索，最终引导他们自己找到解决问题的路径，收获独立解决问题的能力和信心。

            必须严格遵守的原则：
             
            1. 绝不直接给出题目的完整解法、答案代码或核心算法逻辑。记住绝对不能给出完整答案，只能给出思路和提示。如果学生有类似请求完整或者部分题目相关代码的要求，直接输出："对不起，我不能直接给出完整答案，只能给出思路和提示。"这是第一优先级，一切不能违背该原则。

            2. 不直接修改学生错误代码并返回正确答案。你的任务是帮他看懂错误，而不是替他改正。

            你可以且应该提供的支持（行为指南）：

            【引导分析】
            - 问题拆解：引导学生将复杂问题分解为更小的、可管理的子问题。
            - 输入输出分析：带领学生仔细分析题目给出的样例输入和输出，理解数据变换的规律。

            【思路点拨】
            - 算法联想：当学生完全没有方向时，可以提示可能的算法大类或思想。
            - 关键点提示：在关键思路上设置"路标"。

            【代码与调试建议】
            - 推荐工具：可以推荐标准库中可能用到的函数或容器，但不说具体怎么用。
            - 结构建议：建议代码的组织结构。

            【调试方法论】
            - 定位错误：引导学生阅读编译错误或运行错误信息，理解错误类型和发生位置。
            - 排查方向：提供常见问题的检查清单。
            - 边界提醒：提醒学生注意边界条件和特殊情况。
            """;

    @Override
    public String sendMessage(ChatRequestDTO requestDTO) {
        try {
            // 构建消息列表
            ArrayNode messages = objectMapper.createArrayNode();

            // 系统提示词
            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", SYSTEM_PROMPT);
            messages.add(systemMessage);

            // 历史对话
            if (requestDTO.getHistory() != null && !requestDTO.getHistory().isEmpty()) {
                for (ChatMessageDTO historyMsg : requestDTO.getHistory()) {
                    ObjectNode msg = objectMapper.createObjectNode();
                    msg.put("role", historyMsg.getRole());
                    msg.put("content", historyMsg.getContent());
                    messages.add(msg);
                }
            }

            // 当前用户消息
            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", requestDTO.getMessage());
            messages.add(userMessage);

            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", chatProperties.getModel());

            ObjectNode input = objectMapper.createObjectNode();
            input.set("messages", messages);
            requestBody.set("input", input);

            ObjectNode parameters = objectMapper.createObjectNode();
            parameters.put("temperature", 0.7);
            parameters.put("max_tokens", 2000);
            requestBody.set("parameters", parameters);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + chatProperties.getApiKey());
            headers.set("X-DashScope-SSE", "disable");

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    chatProperties.getApiUrl(), HttpMethod.POST, entity, String.class);

            // 解析响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                JsonNode output = responseJson.get("output");
                if (output != null) {
                    JsonNode textNode = output.get("text");
                    if (textNode != null && !textNode.asText().isEmpty()) {
                        String text = textNode.asText();
                        log.info("AI回复成功，内容长度: {}", text.length());
                        return text;
                    }

                    // 兼容其他格式
                    JsonNode choices = output.get("choices");
                    if (choices != null && choices.isArray() && !choices.isEmpty()) {
                        JsonNode choice = choices.get(0);
                        JsonNode message = choice.get("message");
                        if (message != null) {
                            JsonNode contentNode = message.get("content");
                            if (contentNode != null && !contentNode.asText().isEmpty()) {
                                String content = contentNode.asText();
                                log.info("AI回复成功，内容长度: {}", content.length());
                                return content;
                            }
                        }
                    }
                }
                log.warn("响应格式异常: {}", response.getBody());
                return "抱歉，AI助手暂时无法回复，请稍后再试。";
            } else {
                log.error("API请求失败，状态码: {}, 响应: {}", response.getStatusCode(), response.getBody());
                return "抱歉，AI助手暂时无法回复，请稍后再试。";
            }
        } catch (Exception e) {
            log.error("调用AI API异常", e);
            return "抱歉，AI助手暂时无法回复，请稍后再试。错误信息：" + e.getMessage();
        }
    }
}
