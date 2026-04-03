package com.mrs.shakes.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

import java.util.List;

@Data
@Builder
@ToString
public class PagedStoryResponse {
    private String title;
    @Singular // 이 어노테이션을 추가하면 빌더에서 .page() 메서드를 바로 사용 가능합니다.
    private List<Page> pages;

    @Data
    @Builder
    @ToString
    public static class Page {
        private int pageNumber;
        private String text;           // 해당 페이지의 한국어 동화 텍스트 (2~4문장)
        private String imagePrompt;    // 영어 이미지 생성 프롬프트
        private String background;    // 영어 이미지 생성 프롬프트
        private String action;    // 영어 이미지 생성 프롬프트
        private String mood;    // 영어 이미지 생성 프롬프트
        private String imageUrl;
 
    }
}