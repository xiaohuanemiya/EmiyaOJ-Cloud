package com.emiyaoj.problem.service;

import com.emiyaoj.problem.config.AppFileProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProblemImageUrlResolver {

    private static final List<String> LEGACY_BASE_URLS = List.of(
            "http://127.0.0.1:9000",
            "http://localhost:9000",
            "http://minio:9000"
    );

    private final AppFileProperties fileProperties;

    public String buildPublicUrl(String objectKey) {
        String normalizedObjectKey = normalizeObjectKey(objectKey);
        if (!StringUtils.hasText(normalizedObjectKey)) {
            return objectKey;
        }
        return trimTrailingSlash(fileProperties.getPublicBaseUrl())
                + "/" + trimSlashes(fileProperties.getBucket())
                + "/" + normalizedObjectKey;
    }

    public String rewriteLegacyContentUrls(String content) {
        if (!StringUtils.hasText(content)) {
            return content;
        }
        String result = content;
        String publicPrefix = trimTrailingSlash(fileProperties.getPublicBaseUrl())
                + "/" + trimSlashes(fileProperties.getBucket()) + "/";
        for (String baseUrl : LEGACY_BASE_URLS) {
            result = result.replace(trimTrailingSlash(baseUrl) + "/" + trimSlashes(fileProperties.getBucket()) + "/",
                    publicPrefix);
        }
        result = result.replace("](/" + trimSlashes(fileProperties.getBucket()) + "/", "](" + publicPrefix);
        result = result.replace("\"/" + trimSlashes(fileProperties.getBucket()) + "/", "\"" + publicPrefix);
        result = result.replace("'/" + trimSlashes(fileProperties.getBucket()) + "/", "'" + publicPrefix);
        return result;
    }

    private String normalizeObjectKey(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return objectKey;
        }
        String normalized = objectKey.trim();
        String bucketPrefix = trimSlashes(fileProperties.getBucket()) + "/";
        for (String baseUrl : LEGACY_BASE_URLS) {
            String legacyPrefix = trimTrailingSlash(baseUrl) + "/" + bucketPrefix;
            if (normalized.startsWith(legacyPrefix)) {
                normalized = normalized.substring(legacyPrefix.length());
                break;
            }
        }
        normalized = trimLeadingSlash(normalized);
        if (normalized.startsWith(bucketPrefix)) {
            normalized = normalized.substring(bucketPrefix.length());
        }
        return normalized;
    }

    private String trimTrailingSlash(String value) {
        return value == null ? "" : value.replaceAll("/+$", "");
    }

    private String trimLeadingSlash(String value) {
        return value == null ? null : value.replaceAll("^/+", "");
    }

    private String trimSlashes(String value) {
        return trimLeadingSlash(trimTrailingSlash(value));
    }
}
