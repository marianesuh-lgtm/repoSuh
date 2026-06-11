package com.mrs.shakes.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@RestController
@CrossOrigin(origins = "https://comfyui.myshakes.cc") // 프런트엔드 주소 허용
@Slf4j
public class ImageProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/api/proxy/image")
    public ResponseEntity<byte[]> proxyImage(@RequestParam("url") String imageUrl) {
        try {
            // 외부 ComfyUI 이미지 주소에서 이미지 바이너리를 직접 읽어옴
            byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
     
    	    log.info("proxyImage: {}", imageUrl);
    	    log.info("proxyImage: {}", imageBytes);
            
            // 이미지 확장자에 맞게 Content-Type 설정 (PNG 기준, 필요시 가변 처리)
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}