package com.mrs.shakes.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.ollama.api.OllamaChatOptions;

@Configuration
public class AiConfig {

	private final ShakesProperties shakesProperties;

    // 생성자 주입
    public AiConfig(ShakesProperties shakesProperties) {
        this.shakesProperties = shakesProperties;
    }	
	
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        // ChatClient.Builder는 Spring AI가 자동으로 제공합니다.
        // 이를 통해 실제 AI 통신을 담당할 클라이언트를 생성합니다.
    	OllamaChatOptions options = new OllamaChatOptions(); // 직접 생성
        options.setModel("gemma2"); // 세터(Setter) 메서드로 모델 설정
        
        return builder
            .defaultOptions(options)
            .build();
//        return builder
//    	        .defaultOptions(OllamaChatOptions.create().withModel("gemma2")) // 여기서 강제 고정!
//    	        .build();    
        
    }
}
