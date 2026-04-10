package com.mrs.shakes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class StoryChoiceDTO {
	private String stepCode;      // 기, 승, 전, 결
    private String categoryKey;   // character, place 등
    private String optionCode;    // RABBIT, PL001 등
    private String label;
    private String emoji;
    private String code;    // RABBIT, PL001 등
}
