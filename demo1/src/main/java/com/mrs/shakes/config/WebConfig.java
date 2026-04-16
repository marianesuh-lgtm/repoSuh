package com.mrs.shakes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://myshakes.ddns.net",          // 기존
                    "http://myshakes.ddns.net:5173",     // ← 이 줄 추가 (필수!)
                    "http://localhost:5173"              // 개발용
                )
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .exposedHeaders("Authorization");   // 필요 시
    }
}
 
