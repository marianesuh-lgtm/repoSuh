package com.mrs.shakes.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter @Setter @ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class RawStoryResponse {
    private String title;
    private List<RawPageResponse> pages;

    @Getter @Setter @ToString
    public static class RawPageResponse {
        private int pageNumber;
        private String phase;      // 기, 승, 전, 결
        private String content;    // 1차 줄거리 초안
        private String raw_image_keywords; // 프롬프트에 정의한 필드명과 일치해야 함
    }
}
