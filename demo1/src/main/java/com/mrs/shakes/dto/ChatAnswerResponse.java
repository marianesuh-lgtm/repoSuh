package com.mrs.shakes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자에게 반환할 답변 (REST 출력)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAnswerResponse {

    private String answer;
    private String model;
}
