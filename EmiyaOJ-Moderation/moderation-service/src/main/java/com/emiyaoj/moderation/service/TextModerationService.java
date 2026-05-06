package com.emiyaoj.moderation.service;

import com.emiyaoj.moderation.config.ModerationProperties;
import com.emiyaoj.moderation.dto.AuditStatus;
import com.emiyaoj.moderation.dto.ModerationResultDTO;
import com.emiyaoj.moderation.dto.ModerationTaskMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TextModerationService {

    private final com.aliyun.imageaudit20191230.Client imageAuditClient;
    private final ModerationProperties properties;
    private final ObjectMapper objectMapper;

    public ModerationResultDTO moderate(ModerationTaskMessage message) {
        try {
            String text = buildText(message);
            if (!StringUtils.hasText(text)) {
                return result(message, AuditStatus.APPROVED, "pass", "", "Empty text");
            }

            logAliyunCredentialDiagnostics(message);
            com.aliyun.imageaudit20191230.models.ScanTextRequest scanTextRequest = buildRequest(text);
            com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
            com.aliyun.imageaudit20191230.models.ScanTextResponse response =
                    imageAuditClient.scanTextWithOptions(scanTextRequest, runtime);

            return parseResponse(message, response);
        } catch (Exception e) {
            log.warn("Text moderation failed, taskId={}, targetType={}, targetId={}",
                    message.getTaskId(), message.getTargetType(), message.getTargetId(), e);
            return result(message, AuditStatus.MANUAL_REVIEW, "review", "",
                    "ScanText exception: " + safeMessage(e.getMessage()));
        }
    }

    private com.aliyun.imageaudit20191230.models.ScanTextRequest buildRequest(String text) {
        List<com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestLabels> labels =
                properties.getLabels().stream()
                        .map(label -> new com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestLabels()
                                .setLabel(label))
                        .toList();
        com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestTasks task =
                new com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestTasks()
                        .setContent(text);
        return new com.aliyun.imageaudit20191230.models.ScanTextRequest()
                .setTasks(List.of(task))
                .setLabels(labels);
    }

    private ModerationResultDTO parseResponse(ModerationTaskMessage message, Object response) {
        JsonNode root = objectMapper.valueToTree(response);
        JsonNode body = child(root, "body", "Body");
        if (body.isMissingNode()) {
            body = root;
        }
        JsonNode data = child(body, "data", "Data");
        JsonNode elements = child(data, "elements", "Elements");
        if (!elements.isArray() || elements.isEmpty()) {
            return result(message, AuditStatus.MANUAL_REVIEW, "review", "",
                    "ScanText returned no elements");
        }

        JsonNode results = child(elements.get(0), "results", "Results");
        if (!results.isArray() || results.isEmpty()) {
            return result(message, AuditStatus.MANUAL_REVIEW, "review", "",
                    "ScanText returned empty results");
        }

        boolean hasBlock = false;
        boolean hasReview = false;
        boolean hasPass = false;
        Set<String> labels = new LinkedHashSet<>();
        List<String> contexts = new ArrayList<>();

        for (JsonNode result : results) {
            String suggestion = text(result, "suggestion", "Suggestion");
            String label = text(result, "label", "Label");
            if (StringUtils.hasText(label)) {
                labels.add(label);
            }
            if ("block".equalsIgnoreCase(suggestion)) {
                hasBlock = true;
            } else if ("review".equalsIgnoreCase(suggestion)) {
                hasReview = true;
            } else if ("pass".equalsIgnoreCase(suggestion)) {
                hasPass = true;
            } else {
                hasReview = true;
            }
            collectDetails(result, labels, contexts);
        }

        String labelsText = trim(String.join(",", labels), 512);
        String contextsText = trim(String.join(" | ", contexts), 1000);
        if (hasBlock) {
            return result(message, AuditStatus.REJECTED, "block", labelsText,
                    buildReason("block", labelsText, contextsText));
        }
        if (hasReview || !hasPass) {
            return result(message, AuditStatus.MANUAL_REVIEW, "review", labelsText,
                    buildReason("review", labelsText, contextsText));
        }
        return result(message, AuditStatus.APPROVED, "pass", labelsText, "ScanText pass");
    }

    private void collectDetails(JsonNode result, Set<String> labels, List<String> contexts) {
        JsonNode details = child(result, "details", "Details");
        if (!details.isArray()) {
            return;
        }
        for (JsonNode detail : details) {
            String detailLabel = text(detail, "label", "Label");
            if (StringUtils.hasText(detailLabel)) {
                labels.add(detailLabel);
            }
            JsonNode detailContexts = child(detail, "contexts", "Contexts");
            if (!detailContexts.isArray()) {
                continue;
            }
            for (JsonNode context : detailContexts) {
                String hit = text(context, "context", "Context");
                if (StringUtils.hasText(hit)) {
                    contexts.add(hit);
                }
            }
        }
    }

    private String buildText(ModerationTaskMessage message) {
        if (message.getTargetType() == null) {
            return message.getContent();
        }
        return switch (message.getTargetType()) {
            case BLOG -> "title: " + nullToEmpty(message.getTitle()) + "\ncontent: " + nullToEmpty(message.getContent());
            case COMMENT -> nullToEmpty(message.getContent());
        };
    }

    private ModerationResultDTO result(ModerationTaskMessage message,
                                       AuditStatus status,
                                       String suggestion,
                                       String labels,
                                       String reason) {
        ModerationResultDTO result = new ModerationResultDTO();
        result.setTaskId(message.getTaskId());
        result.setTargetType(message.getTargetType());
        result.setTargetId(message.getTargetId());
        result.setAuditStatus(status.getCode());
        result.setSuggestion(suggestion);
        result.setLabels(trim(labels, 512));
        result.setReason(trim(reason, 1000));
        result.setAuditTime(LocalDateTime.now());
        return result;
    }

    private String buildReason(String suggestion, String labels, String contexts) {
        String reason = "suggestion=" + suggestion;
        if (StringUtils.hasText(labels)) {
            reason += "; labels=" + labels;
        }
        if (StringUtils.hasText(contexts)) {
            reason += "; contexts=" + contexts;
        }
        return reason;
    }

    private JsonNode child(JsonNode node, String... names) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return objectMapper.missingNode();
        }
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null) {
                return value;
            }
        }
        return objectMapper.missingNode();
    }

    private String text(JsonNode node, String... names) {
        JsonNode value = child(node, names);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        return value.asText();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String safeMessage(String message) {
        return message == null ? "" : message;
    }

    private void logAliyunCredentialDiagnostics(ModerationTaskMessage message) {
        String accessKeyId = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID");
        String accessKeySecret = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET");
        log.info("Aliyun ScanText credential check, taskId={}, targetType={}, targetId={}, accessKeyId={}, accessKeyIdConfigured={}, accessKeySecret={}, accessKeySecretConfigured={}, accessKeySecretLength={}",
                message.getTaskId(),
                message.getTargetType(),
                message.getTargetId(),
                maskSecret(accessKeyId),
                StringUtils.hasText(accessKeyId),
                maskSecret(accessKeySecret),
                StringUtils.hasText(accessKeySecret),
                accessKeySecret == null ? 0 : accessKeySecret.length());
    }

    private String maskSecret(String value) {
        if (!StringUtils.hasText(value)) {
            return "<empty>";
        }
        if (value.length() <= 8) {
            return "*".repeat(value.length());
        }
        return value.substring(0, 4) + "*".repeat(Math.min(value.length() - 8, 16)) + value.substring(value.length() - 4);
    }

    private String trim(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
