package com.mrs.shakes.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mrs.shakes.dto.GenerateBookRequest;
import com.mrs.shakes.dto.PagedStoryResponse;
import com.mrs.shakes.dto.StoryParameterDTO;
import com.mrs.shakes.mapper.StoryContentMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoryGenerationService {

    private final StoryContentMapper mapper;

    public PagedStoryResponse getGeneratePages(GenerateBookRequest request) {
    	
    	StoryParameterDTO dto  = setParameter(request);
    	PagedStoryResponse rslt = mapper.getStoryContent(dto);
    	
    	dto.setStoryId(rslt.getStoryId());
        
    	List<PagedStoryResponse.Page> rsltPages = mapper.getStoryPages(dto);

    	rslt.setTotalPages(rsltPages.size());
    	rslt.setPages(rsltPages);
    	
    	return rslt;
    }
    
    private StoryParameterDTO setParameter(GenerateBookRequest request) {
    	StoryParameterDTO content = new StoryParameterDTO();
    	
       var selections = request.getSelections();
        
        // 구조에 맞게 mapping (예시)
        content.setCharCode(selections.get기().getCharacter().getCode()); 
        content.setPlaCode(selections.get기().getPlace().getCode());
        content.setModCode(selections.get기().getMood().getCode());
        content.setEveCode(selections.get승().getEvent().getCode()); 
        content.setComCode(selections.get승().getCompanion().getCode()); 
        content.setProCode(selections.get전().getProblem().getCode()); 
        content.setActCode(selections.get전().getTryAction().getCode()); 
        content.setSolCode(selections.get결().getSolution().getCode()); 
        content.setEndCode(selections.get결().getEnding().getCode()); 
   	
    	
    	return content ;
    }
    
}