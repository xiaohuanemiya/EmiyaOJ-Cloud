package com.emiyaoj.judge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${go-judge.response-max-in-memory-size:201326592}")
    private int goJudgeResponseMaxInMemorySize;

    @Bean
    public WebClient.Builder webClientBuilder() {
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs()
                        .maxInMemorySize(goJudgeResponseMaxInMemorySize))
                .build();
        return WebClient.builder().exchangeStrategies(exchangeStrategies);
    }
}
