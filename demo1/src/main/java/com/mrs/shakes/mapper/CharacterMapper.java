package com.mrs.shakes.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.mrs.shakes.dto.CharacterDTO;

@Mapper
public interface CharacterMapper {
	
	CharacterDTO retrieveCharacter(String charId);

	CharacterDTO retrieveSubCharacter(String subId);

}
