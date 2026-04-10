package com.mrs.shakes.service;

import java.util.List;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

import com.mrs.shakes.client.OllamaClient;
import com.mrs.shakes.domain.character.CharacterRepository;
import com.mrs.shakes.dto.StoryChoiceDTO;
import com.mrs.shakes.mapper.StoryContentMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor

public class StoryContentService {

	   private final StoryContentMapper mapper;

	   List<StoryChoiceDTO> getStoryCodeMaster(){
		   return mapper.getStoryCodeMaster();
	   } 
	

}
