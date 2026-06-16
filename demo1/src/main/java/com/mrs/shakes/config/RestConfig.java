package com.mrs.shakes.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {
    
    @SuppressWarnings("removal")
	@Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(120))
                .setReadTimeout(Duration.ofMinutes(5)) // AI 생성 시간을 고려해 넉넉히
                .build();
    }
}
