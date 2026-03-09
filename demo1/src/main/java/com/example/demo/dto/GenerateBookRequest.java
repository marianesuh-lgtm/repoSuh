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
            private String character;   // 기 단계에서만 의미 있음
            private String place;       // 기 단계에서만 의미 있음
            private String mood;        // 기 단계에서만 의미 있음

            private String event;       // 승 단계
            private String companion;   // 승 단계

            private String problem;     // 전 단계
            private String tryAction;   // 전 단계 (try는 예약어이므로 tryAction으로 변경)

            private String solution;    // 결 단계
            private String ending;      // 결 단계
        }
    }
}