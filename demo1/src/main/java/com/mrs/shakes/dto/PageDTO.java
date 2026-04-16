package com.mrs.shakes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageDTO {
    
    @JsonProperty("page_id")
    private String pageId;

    @JsonProperty("story_id")
    private String storyId;

    @JsonProperty("char_img")
    private String charImg;
    
	@JsonProperty("page_no")
    private int pageNo;

    @JsonProperty("content")
    private String content;

    @JsonProperty("refined_text")
    private String refinedText;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("image_prompt")
    private String imagePrompt;

    // 기본 생성자 (Jackson 역직렬화에 필요)
    public PageDTO() {}

    public PageDTO( String pageId, String storyId, int pageNo, String charImg, String content, String refinedText, String imageUrl, String imagePrompt) {
        this.pageId = pageId;
        this.storyId = storyId;
        this.pageNo = pageNo;
        this.charImg = charImg;
        this.content = content;
        this.refinedText = refinedText;
        this.imageUrl = imageUrl;
        this.imagePrompt = imagePrompt;
    }
}
