package com.mrs.shakes.dto;

import java.util.List;

import com.mrs.shakes.dto.BookResponse.PageItem;

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
	private String weakness ; 
	private String artStyle ;
	private String negative ;
	private String personalityTraits ;
	private String background ; 
	private String mood ; 
	private String problem ; 
	private String urlImg ; 
	private String subCharacter;
	private String subId;
	private String subTitle;
	private String subAppearance;
	private String subAntiYn;
	private String subNegative;
	private String subUrlImg ; 
	private String subCharacteristic;
	
}
