package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class PagedStoryResponse {
    private String title;
    private List<Page> pages;

    @Getter
    @Builder
    @ToString
    public static class Page {
        private int pageNumber;
        private String text;           // 해당 페이지의 한국어 동화 텍스트 (2~4문장)
        private String imagePrompt;    // 영어 이미지 생성 프롬프트
    }
}