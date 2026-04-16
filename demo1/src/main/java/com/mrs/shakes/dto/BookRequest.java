package com.mrs.shakes.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class BookRequest {

	private String name;
    private int age;
    private String likes;
    private String theme;
    private String storyId;
    private String verifyYn ;
    private String imagePrompt ;
    private String pageId ;
    private String charImg ;
    private String imageUrl ;
    private String content ;
}
