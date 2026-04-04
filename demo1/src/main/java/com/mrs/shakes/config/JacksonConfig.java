package com.mrs.shakes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 1. DTO에 없는 필드가 JSON에 있어도 에러 내지 않고 무시함 (중요!)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 2. 날짜/시간 데이터를 ISO-8601 형식으로 예쁘게 처리
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 3. snake_case(raw_image_keywords)와 camelCase 간 자동 매핑이 필요하다면 설정
        // mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        
        return mapper;
    }
}