package com.mrs.shakes.controller;

import com.mrs.shakes.dto.ImageGenerationRequest;
import com.mrs.shakes.dto.ImageGenerationResponse;
import com.mrs.shakes.service.ImageGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
@Slf4j
@RequiredArgsConstructor
public class ImageGenerationController {

    private final ImageGenerationService imageGenerationService;

    /**
     * POST /api/images/generate
     * Vue에서 받은 사용자 입력(JSON)을 기반으로:
     * - Ollama로 상세 묘사 프롬프트 생성
     * - Ollama로 ComfyUI용 SD 프롬프트 추출
     * - ComfyUI로 이미지 생성
     * - 이미지 URL 목록 반환
     */
    @PostMapping("/generate")
    public ResponseEntity<ImageGenerationResponse> generate(@RequestBody ImageGenerationRequest request) {
        try {
            log.info("Image generate request: topic={}, style={}, background={}",
                    request.getTopic(), request.getStyle(), request.getBackground());
            ImageGenerationResponse res = imageGenerationService.generate(request);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Image generation failed: {}", e.getMessage(), e);
            ImageGenerationResponse res = new ImageGenerationResponse();
            res.setSuccess(false);
            res.setMessage(e.getMessage());
            res.setDetailedPrompt(null);
            res.setPositivePrompt(null);
            res.setNegativePrompt(null);
            res.setImageUrls(null);
            return ResponseEntity.badRequest().body(res);
        }
    }
}

