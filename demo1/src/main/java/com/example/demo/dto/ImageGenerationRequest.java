package com.example.demo.dto;

import lombok.Data;

@Data
public class ImageGenerationRequest {

    /** 주제(무엇을 그릴지) */
    private String topic;

    /** 스타일(예: watercolor, pixar, anime, oil painting, pixel art 등) */
    private String style;

    /** 배경(장소/배경 요소) */
    private String background;

    /** 분위기(예: warm, dreamy, cinematic, cute 등) */
    private String mood;

    /** 조명(예: soft lighting, golden hour, studio lighting 등) */
    private String lighting;

    /** 색감(예: pastel, vibrant, monochrome 등) */
    private String colorPalette;

    /** 구도/카메라(예: close-up, wide shot, top-down 등) */
    private String composition;

    /** 추가 요구사항 */
    private String extra;

    /** 사용자가 지정하는 네거티브 프롬프트(선택) */
    private String negative;

    /** Ollama 모델(선택). 미지정 시 application.properties의 ollama.default-model 사용 */
    private String ollamaModel;

    /** ComfyUI 파라미터 오버라이드(선택) */
    private Integer width;
    private Integer height;
    private Integer steps;
    private Integer seed;
    private Double cfg;
    private Integer batchSize;
    private String ckptName;
    private String samplerName;
    private String scheduler;
    private Double denoise;
}

