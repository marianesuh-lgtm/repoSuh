package com.mrs.shakes.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class StoryRequestDTO {

    private String userId;           // 작성자 ID (필수)
    private String storyId;           // 작성자 ID (필수)
    private String title;            // 동화 제목
    private String mainCharacter;    // 주인공 이름/종류
    
    // 프론트엔드에서 보낸 선택된 코드들 (문자열 또는 객체 리스트)
    // 여기서는 범용성을 위해 Object나 String으로 선언합니다.
    private String selectedCodes;    
    
    private String status;           // 현재 상태 (예: START, PROGRESS, COMPLETED)
    private String isFavorite;           // 현재 상태 (예: START, PROGRESS, COMPLETED)
    
    private Integer rating;          // 별점 (초기 저장시에는 생략 가능)
    private String feedbackComment;  // 피드백
    
}
