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
}
