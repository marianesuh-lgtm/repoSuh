package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.example.demo.dto.CharacterDTO;

@Mapper
public interface CharacterMapper {
	
	CharacterDTO retrieveCharacter(String charId);


}
