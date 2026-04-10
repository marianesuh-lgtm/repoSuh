package com.mrs.shakes.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrs.shakes.client.OllamaClient;
import com.mrs.shakes.config.ShakesProperties;
import com.mrs.shakes.domain.character.CharacterRepository;
import com.mrs.shakes.domain.story.StoryMaster;
import com.mrs.shakes.dto.AdminStoryDTO;
import com.mrs.shakes.dto.CharacterDTO;
import com.mrs.shakes.dto.GenerateBookRequest;
import com.mrs.shakes.dto.StoryParameterDTO;
import com.mrs.shakes.infrastructure.prompt.PromptProvider;
import com.mrs.shakes.mapper.CharacterMapper;
import com.mrs.shakes.mapper.StoryContentMapper;
import com.mrs.shakes.repository.StoryMasterRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j

public class AdminStoryService {

//    private final StoryMasterRepository storyMasterRepository;
//    private final OllamaClient ollamaClient; // Ollama와 통신하는 커스텀 클라이언트
//    private final PromptProvider promptProvider; // .st 파일을 읽어주는 컴포넌트
//    private final ObjectMapper objectMapper;
//    private final CharacterService characterService ;
//    private final IncrementalStoryService refineService;
//    private final String imgUrl = "http://myShakes.ddns.net:8080/images/characters/";
//	private final RestTemplate restTemplate = new RestTemplate();
//
//    private final ShakesProperties properties; // 생성자 주입
//    @Autowired
//    private ResourceLoader resourceLoader;
    
    private final StoryContentMapper storyMapper;

//    @JsonProperty("raw_image_keywords")
//    private String rawImageKeywords;
    
    public List<AdminStoryDTO> getAdminStories(GenerateBookRequest request) {
    	StoryParameterDTO dto = new StoryParameterDTO();
        return storyMapper.getAdminStories(dto);
    }    
   

}
