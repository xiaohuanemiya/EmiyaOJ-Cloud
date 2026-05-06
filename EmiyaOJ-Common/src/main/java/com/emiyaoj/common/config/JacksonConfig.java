package com.emiyaoj.common.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 全局 Jackson 序列化配置
 * <p>
 * 统一 JSON 序列化/反序列化行为，包括：
 * <ul>
 *     <li>Long → String（防止前端 JS 精度丢失）</li>
 *     <li>Java 8 日期时间统一格式</li>
 *     <li>忽略未知属性（反序列化容错）</li>
 *     <li>空对象不报错</li>
 * </ul>
 */
@Configuration
@ConditionalOnClass(ObjectMapper.class)
public class JacksonConfig {

    /** 日期时间格式常量 */
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String ISO_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";

    /**
     * 自定义 Jackson ObjectMapper 构建器
     * <p>
     * 通过 {@link Jackson2ObjectMapperBuilderCustomizer} 而非直接注入 {@link ObjectMapper}，
     * 确保不覆盖 Spring Boot 自动配置的其他定制。
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            // ===== Long → String（解决前端 JS Number 精度丢失问题） =====
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);

            // ===== Java 8 日期时间序列化/反序列化 =====
            JavaTimeModule javaTimeModule = new JavaTimeModule();

            // LocalDateTime
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
            javaTimeModule.addDeserializer(LocalDateTime.class, localDateTimeDeserializer());

            // LocalDate
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
            javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
            javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));

            // LocalTime
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN);
            javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter));
            javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));

            builder.modules(javaTimeModule);

            // ===== 反序列化容错 =====
            // 忽略 JSON 中存在但 Java 对象中不存在的属性
            builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            // 空对象不抛异常
            builder.featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            // 日期不序列化为时间戳
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }

    private JsonDeserializer<LocalDateTime> localDateTimeDeserializer() {
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern(DATE_TIME_PATTERN),
                DateTimeFormatter.ofPattern(ISO_DATE_TIME_PATTERN),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        );
        return new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
                String value = parser.getText();
                if (value == null || value.isBlank()) {
                    return null;
                }
                for (DateTimeFormatter formatter : formatters) {
                    try {
                        return LocalDateTime.parse(value, formatter);
                    } catch (DateTimeParseException ignored) {
                        // Try the next supported request format.
                    }
                }
                return (LocalDateTime) context.handleWeirdStringValue(
                        LocalDateTime.class,
                        value,
                        "Expected date time format yyyy-MM-dd HH:mm:ss or yyyy-MM-dd'T'HH:mm:ss"
                );
            }
        };
    }
}
