package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {

    private boolean success;          // 성공 여부 (true/false)
    private String message;           // 성공 시 "생성 완료", 실패 시 에러 메시지
    private String story;             // 전체 스토리 텍스트 (Ollama에서 받은 원문)
    private List<PageItem> pages;     // 페이지별 데이터 (텍스트 + 이미지 URL)

    // 페이지별 상세 구조 (선택)
    @Data
    @Builder
    public static class PageItem {
        private int pageNumber;       // 1, 2, 3...
        private String text;          // 해당 페이지 텍스트
        private String imageUrl;      // ComfyUI에서 생성된 이미지 URL (또는 base64)
        private String promptUsed;    // 사용된 프롬프트 (디버깅용, 옵션)
    }

    // 에러 응답용 정적 팩토리 메서드 (편의용)
    public static BookResponse error(String errorMessage) {
    	BookResponse response = new BookResponse();
        response.setSuccess(false);
        response.setMessage(errorMessage);
        response.setStory(null);
        response.setPages(null);
        return response;
    }

    // 성공 응답용 정적 팩토리 메서드
    public static BookResponse success(String story, List<PageItem> pages) {
    	BookResponse response = new BookResponse();
        response.setSuccess(true);
        response.setMessage("책 생성 완료");
        response.setStory(story);
        response.setPages(pages);
        return response;   
        }

 // 새로 추가: 간단한 imageUrls만 받는 버전 (임시 또는 최소 버전용)

    public static BookResponse success2(String story, List<BookResponse.PageItem> items) {
        BookResponse r = new BookResponse();
        r.setSuccess(true);
        r.setMessage("책 생성 완료");
        r.setStory(story);

        // imageUrls를 PageItem으로 변환 (pages가 null이 아닌 최소 구조)
        r.setPages(items);

        return r;
    }

}
