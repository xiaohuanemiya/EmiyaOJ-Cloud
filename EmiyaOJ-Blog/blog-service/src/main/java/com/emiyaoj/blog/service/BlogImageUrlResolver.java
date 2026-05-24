package com.emiyaoj.blog.service;

import com.emiyaoj.blog.config.AppFileProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BlogImageUrlResolver {

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
        return resolvePublicBaseUrl()
                + "/" + trimSlashes(fileProperties.getBucket())
                + "/" + normalizedObjectKey;
    }

    public String rewriteLegacyContentUrls(String content) {
        if (!StringUtils.hasText(content)) {
            return content;
        }
        String result = content;
        String publicPrefix = resolvePublicBaseUrl()
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

    private String resolvePublicBaseUrl() {
        if (!Boolean.TRUE.equals(fileProperties.getDynamicBaseUrl())) {
            return trimTrailingSlash(fileProperties.getPublicBaseUrl());
        }
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return trimTrailingSlash(fileProperties.getPublicBaseUrl());
        }
        String host = firstHeaderValue(request, "X-Forwarded-Host");
        if (!StringUtils.hasText(host)) {
            host = request.getHeader("Host");
        }
        if (!StringUtils.hasText(host)) {
            host = request.getServerName();
        }

        String scheme = firstHeaderValue(request, "X-Forwarded-Proto");
        if (!StringUtils.hasText(scheme)) {
            scheme = request.getScheme();
        }

        String hostWithoutPort = stripPort(host);
        Integer publicPort = fileProperties.getPublicPort();
        if (publicPort == null || isDefaultPort(scheme, publicPort)) {
            return scheme + "://" + hostWithoutPort;
        }
        return scheme + "://" + hostWithoutPort + ":" + publicPort;
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String firstHeaderValue(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.split(",")[0].trim();
    }

    private String stripPort(String host) {
        if (!StringUtils.hasText(host)) {
            return host;
        }
        String trimmed = host.trim();
        if (trimmed.startsWith("[") && trimmed.contains("]")) {
            return trimmed.substring(0, trimmed.indexOf(']') + 1);
        }
        int colonIndex = trimmed.indexOf(':');
        return colonIndex < 0 ? trimmed : trimmed.substring(0, colonIndex);
    }

    private boolean isDefaultPort(String scheme, int port) {
        return ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
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
