package com.mrs.shakes.dto;

import com.mrs.shakes.entity.Child;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildResponse {
    private Long childId;
    private String name;
    private int age;
    private String gender;

    // Entity를 DTO로 변환하는 정적 팩토리 메서드 (선택 사항)
    public static ChildResponse from(Child child) {
        return ChildResponse.builder()
                .childId(child.getChildId())
                .name(child.getName())
                .age(child.getAge())
                .gender(child.getGender())
                .build();
    }
}