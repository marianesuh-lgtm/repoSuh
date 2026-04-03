package com.mrs.shakes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class AppConfig {

	@Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .build();
    }

	@Bean
	public WebClient ollamaWebClient(
			WebClient.Builder builder,
			@Value("${ollama.base-url:http://suhmac.local:11434}") String baseUrl) {
		return builder
				.baseUrl(baseUrl)
				.build();
	}
}