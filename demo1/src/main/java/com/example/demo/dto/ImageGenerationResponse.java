package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class ImageGenerationResponse {

    private boolean success;
    private String message;

    /** Ollama가 만든 상세 묘사 프롬프트 */
    private String detailedPrompt;

    /** ComfyUI(Stable Diffusion)용 positive prompt */
    private String positivePrompt;

    /** ComfyUI(Stable Diffusion)용 negative prompt */
    private String negativePrompt;

    /** 생성된 이미지 URL 목록 */
    private List<String> imageUrls;
}

