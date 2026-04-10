package com.mrs.shakes.dto;

import java.util.List;

import com.mrs.shakes.dto.PagedStoryResponse.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;

@Data
@Builder
@ToString
@NoArgsConstructor  // 추가
@AllArgsConstructor // 추가

public class AdminStoryDTO {
    private String storyId;
    private String title;
	private String charCode;
	private String charDesc;
    private String plaCode;
    private String plaDesc;
    private String modCode;
    private String modDesc;
    private String eveCode;
    private String eveDesc;
    private String comCode;
    private String comDesc;
    private String proCode;
    private String proDesc;
    private String actCode;
    private String actDesc;
    private String solCode;
    private String solDesc;
    private String endCode;
    private String endDesc;
    private String  createdAt;
    private String  summary ;
    private boolean isVerified;
    private String  tags; 

    private List<PageDTO> pages; // JSON이 자동으로 List로 변환되도록 설정 (Jackson 라이브러리)
}
