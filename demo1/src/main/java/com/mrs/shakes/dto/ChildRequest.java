package com.mrs.shakes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Getter               // Service에서 값을 꺼내기 위해 필수
@Setter               // Jackson이 값을 채우기 위해 필요할 수 있음
//@NoArgsConstructor    // 중요: Spring이 JSON을 객체로 만들 때 이 생성자를 씁니다.
@AllArgsConstructor   // 모든 필드 생성자
@ToString             // 로그 확인용
public class ChildRequest {
    @JsonProperty("name")
    private String name;

    @JsonProperty("age")
    private Integer age;

    @JsonProperty("gender")
    private String gender;

    // ... 생성자, getter, setter
}