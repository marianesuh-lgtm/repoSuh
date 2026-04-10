package com.mrs.shakes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class StoryParameterDTO {
	
	private String charCode;
    private String plaCode;
    private String modCode;
    private String eveCode;
    private String comCode;
    private String proCode;
    private String actCode;
    private String solCode;
    private String endCode;
    private String storyId;

}
