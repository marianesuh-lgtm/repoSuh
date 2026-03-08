package com.example.demo.dto;

import java.util.List;

import lombok.Data;

@Data
public class NextSceneResponse {
	
	private  boolean success;
	private  String message;
	private  String nextPart;
	private  String imagePrompt;     // ComfyUI에 보낼 프롬프트
	private  String imageUrl;        // 생성된 이미지 URL (비동기면 null 가능)
	private  List<String> nextChoices;
	private  int currentStep;

}
