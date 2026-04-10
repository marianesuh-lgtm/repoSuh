package com.mrs.shakes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;

import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor  // 추가
@AllArgsConstructor // 추가
public class PagedStoryResponse {
    private String storyId;
    private String title;
    private String sideCharacterAppearance;
    private Integer totalPages;
    @Singular // 이 어노테이션을 추가하면 빌더에서 .page() 메서드를 바로 사용 가능합니다.
    private List<Page> pages;

    @Data
    @Builder
    @ToString
    @NoArgsConstructor  // 추가
    @AllArgsConstructor // 추가
    public static class Page {
        private int pageNumber;
        private String rawText;           // 해당 페이지의 한국어 동화 텍스트 (2~4문장)
        private String text;           // 해당 페이지의 한국어 동화 텍스트 (2~4문장)
        private String imagePrompt;    // 영어 이미지 생성 프롬프트
        private String background;    // 영어 이미지 생성 프롬프트
        private String action;    // 영어 이미지 생성 프롬프트
        private String mood;    // 영어 이미지 생성 프롬프트
        private String imageUrl;
        private String refinedText;           // 해당 페이지의 한국어 동화 텍스트 (2~4문장)
 
    }
}