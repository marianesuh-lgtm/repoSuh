package com.mrs.shakes.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) // JSON에 DTO에 없는 필드가 있어도 에러 방지
public class RefinedStoryResponse {

	private String refined_content;   // 사용자에게 보여줄 텍스트
    private String image_prompt;      // ComfyUI API의 'positive prompt'로 전달할 값
    private List<String> refined_keywords; // 해시태그나 검색용
	private String previousContext;   // 사용자에게 보여줄 텍스트
}
