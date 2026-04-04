package com.mrs.shakes.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data // 롬복 필수 (Setter를 통해 값이 주입됨)
@Configuration
@ConfigurationProperties(prefix = "mrs-shakes") // YAML의 시작 지점 지정
public class ShakesProperties {

    private Ollama ollama = new Ollama(); // 내부 계층 구조
    private Comfy comfy = new Comfy();

    @Data // 롬복 필수 (Setter를 통해 값이 주입됨)
    public static class Ollama {
        private String baseUrl;
        private String model;
        private int timeoutSeconds;
    }

    @Data // 롬복 필수 (Setter를 통해 값이 주입됨)
    public static class Comfy {
        private String baseUrl;
        private String workflowPath;
    }
}
