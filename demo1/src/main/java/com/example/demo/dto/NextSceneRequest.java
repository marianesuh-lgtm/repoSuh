package com.example.demo.dto;

import lombok.Data;

@Data
public class NextSceneRequest {
	
	private String currentStory;
	private  String userChoice;
	private int step;
	private String childName;
	private String gender;
	private int age;
	private String likes;
	private String theme;
	private int maxLength = 200;

}
