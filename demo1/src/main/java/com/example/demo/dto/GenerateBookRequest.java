package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GenerateBookRequest {

    private StorySelections selections;
    private Integer pageCount;
    private String format;
    private Double temperature;
    private Integer maxTokens;

    // 중첩 클래스
    @Getter
    @Setter
    @ToString
    public static class StorySelections {
        private Step 기;
        private Step 승;
        private Step 전;
        private Step 결;

        @Getter
        @Setter
        @ToString
        public static class Step {
            private SelectionItem character;   // 기 단계에서만 의미 있음
            private SelectionItem place;       // 기 단계에서만 의미 있음
            private SelectionItem mood;        // 기 단계에서만 의미 있음

            private SelectionItem event;       // 승 단계
            private SelectionItem companion;   // 승 단계

            private SelectionItem problem;     // 전 단계
            private SelectionItem tryAction;   // 전 단계 (try는 예약어이므로 tryAction으로 변경)

            private SelectionItem solution;    // 결 단계
            private SelectionItem ending;      // 결 단계
        }
        
        
     // 2. code와 label을 동시에 받기 위한 클래스를 정의합니다.
        @Getter
        @Setter
        @ToString
        public static class SelectionItem {
            private String code;
            private String label;
        }
    }
}