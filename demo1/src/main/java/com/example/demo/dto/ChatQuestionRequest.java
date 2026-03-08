package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 질문 요청 (REST 입력)
 */
@Data
@NoArgsConstructor
public class ChatQuestionRequest {

    private String question;

    /** 사용할 Ollama 모델 (선택, 기본값 적용) */
    private String model;
}
