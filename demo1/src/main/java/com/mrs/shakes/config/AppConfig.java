package com.mrs.shakes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import javax.crypto.SecretKey;

@Configuration
public class AppConfig {


	@Value("${jwt.secret}") // application.yml에 정의된 비밀키 문자열
    private String secretString;

    @Bean
    public SecretKey secretKey() {
        // 문자열을 바탕으로 HMAC-SHA 알고리즘에 적합한 SecretKey 객체 생성
        return Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }	
	
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