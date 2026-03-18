package com.example.demo.dto;

import java.util.List;

import com.example.demo.dto.BookResponse.PageItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CharacterDTO {
	
	private String charId ; 
	private String appearance ; 
	private String artStyle ;
	private String negative ;
	private String personalityTraits ;
	private String background ; 
	private String mood ; 
	private String problem ; 
	private String urlImg ; 
	

}
