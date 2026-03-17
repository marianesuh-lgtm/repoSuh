package com.example.demo.util;

public class JsonUtils {

    /**
     * 문자열 내의 특수 문자를 JSON 이스케이프 문자로 변환합니다.
     * (따옴표, 백슬래시, 줄바꿈 등 처리)
     */
    public static String escapeJsonValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")   // 백슬래시
                    .replace("\"", "\\\"")   // 큰따옴표
                    .replace("\b", "\\b")    // 백스페이스
                    .replace("\f", "\\f")    // 폼피드
                    .replace("\n", "\\n")    // 줄바꿈
                    .replace("\r", "\\r")    // 캐리지 리턴
                    .replace("\t", "\\t");   // 탭
    }
}
