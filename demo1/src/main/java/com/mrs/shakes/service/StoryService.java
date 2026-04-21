package com.mrs.shakes.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrs.shakes.client.OllamaClient;
import com.mrs.shakes.config.ShakesProperties;
import com.mrs.shakes.domain.story.StoryContent;
import com.mrs.shakes.domain.story.StoryMaster;
import com.mrs.shakes.domain.story.StoryPage;
import com.mrs.shakes.domain.story.StoryStatus;
import com.mrs.shakes.dto.BookResponse;
import com.mrs.shakes.dto.CharacterDTO;
import com.mrs.shakes.dto.GenerateBookRequest;
import com.mrs.shakes.dto.PagedStoryResponse;
import com.mrs.shakes.dto.RawStoryResponse;
import com.mrs.shakes.dto.RawStoryResponse.RawPageResponse;
import com.mrs.shakes.dto.RefinedStoryResponse;
import com.mrs.shakes.dto.StoryRequest;
import com.mrs.shakes.dto.StoryRequestDTO;
import com.mrs.shakes.entity.UserStory;
import com.mrs.shakes.dto.GenerateBookRequest.StorySelections;
import com.mrs.shakes.dto.PagedStoryResponse.Page;
import com.mrs.shakes.infrastructure.prompt.PromptProvider;
import com.mrs.shakes.repository.StoryMasterRepository;
import com.mrs.shakes.repository.UserStoryRepository;
import com.mrs.shakes.util.JsonUtils;
import com.mrs.shakes.util.ServiceUtil;
import com.mrs.shakes.util.StoryMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j

public class StoryService {

    private final StoryMasterRepository storyMasterRepository;
    private final OllamaClient ollamaClient; // Ollama와 통신하는 커스텀 클라이언트
    private final PromptProvider promptProvider; // .st 파일을 읽어주는 컴포넌트
    private final ObjectMapper objectMapper;
//    private final CharacterService characterService ;
//    private final IncrementalStoryService refineService;
//    private final String imgUrl = "http://myShakes.ddns.net:8080/images/characters/";
	private final RestTemplate restTemplate = new RestTemplate();
	private final ServiceUtil svcUtil;
	private final UserStoryRepository userStoryRepository;
	//private final StoryRequestDTO storyDto;

//    private final ShakesProperties properties; // 생성자 주입
//    @Autowired
//    private ResourceLoader resourceLoader;
    
//    private final StoryMapper storyMapper;

    @JsonProperty("raw_image_keywords")
    private String rawImageKeywords;


    public StoryMaster generateStory(GenerateBookRequest request) {
        // 1. AI 응답 받기
        //String aiResponse = ollamaService.ask(request); 
    	CharacterDTO character = svcUtil.setCharacterDto(request);
        String mood = request.getSelections().get기().getMood().getLabel();
        String weakness = request.getSelections().get전().getProblem().getLabel();

    	int pageCount = request.getPageCount();
    	String context = "";
    	StoryMaster master = this.createStoryDraft(request, character, pageCount);
    	
    	createInitialStory(request, master) ;
    	
    	log.info("master::: {}", master); 
        log.info("초안 생성 완료: master_id={}", master.getId()); 
       	
        // 2. 각 페이지별 2차 Refining 진행
        this.refineStoryPages(request, master, context, character );
        
        return master;
    }    

    
    @Transactional
    public Long createInitialStory(GenerateBookRequest request, StoryMaster master) {
        // 1. StoryMaster 생성
        //StoryMaster master = new StoryMaster();
        //master.setTitle(request.getTitle());
        // ... 기타 설정 ...

        // 2. StoryContent 생성 (화면 선택값 세팅)
        StoryContent content = new StoryContent();
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

        // ... 나머지 eve, com, pro, act, sol, end 코드들 세팅 ...
        
        //content.setSummary(request.getSummary()); // 요약본이 있다면

        // 3. 연관 관계 맺기
        master.setContent(content);

        // 4. 저장 (Cascade 설정으로 master만 저장해도 content가 함께 저장됨)
        return storyMasterRepository.save(master).getId();
    }
    
    @Transactional
    private void getImageUrl(GenerateBookRequest request, StoryMaster master, String context, CharacterDTO character) {
    	
        String mood = request.getSelections().get기().getMood().getLabel();

        // 1. 순서 보장을 위해 페이지 번호순으로 정렬해서 순회 (중요)
        List<StoryPage> pages = master.getPages().stream()
                .sorted(Comparator.comparing(StoryPage::getPageNumber))
                .toList();
    	
	    try {

    	    List<Page> imageUrls = new ArrayList<>();
    	    String workflowJson = new String(svcUtil.loadWorkflow());


           // log.info("workflow::: {}", workflowJson);
            ObjectMapper mapper = new ObjectMapper();

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (StoryPage page : pages) {
            	             	
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // ComfyUI 호출 및 Polling 로직을 이 안으로 이동
                        // (주의: RestTemplate은 Thread-Safe 하므로 공유 가능)
                        String imageUrl = svcUtil.generateAndPollImage(restTemplate, page , mood, character ); 
                        page.setImageUrl(imageUrl); // 객체에 직접 세팅
                    } catch (Exception e) {
                        log.error("이미지 생성 실패: ", e);
                    }
                });
                futures.add(future);
            }

         // 모든 페이지 생성이 끝날 때까지 대기 (최대 타임아웃 설정 권장)
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();            
            
          } catch (Exception e) {
            log.error("동화 생성 실패", e);
          }        

	    storyMasterRepository.save(master) ;
        // @Transactional 어노테이션 덕분에 루프 종료 시 변경 감지(Dirty Checking)로 자동 업데이트 됩니다.
    }    
    
    
    @Transactional
    private void refineStoryPages(GenerateBookRequest request, StoryMaster master, String context, CharacterDTO character) {
    	
      	String previousContext = "";
        String mood = request.getSelections().get기().getMood().getLabel();
   	
    	// 1. 순서 보장을 위해 페이지 번호순으로 정렬해서 순회 (중요)
        List<StoryPage> pages = master.getPages().stream()
                .sorted(Comparator.comparing(StoryPage::getPageNumber))
                .toList();
        int row = 0;
        
        for (StoryPage page : pages) {
        	page.setPreviousContext(previousContext);
        	previousContext =  page.getRawContent();
        }
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        //String refinePrompt = "";
        
       for (StoryPage page : pages) {
            // 1. 리파이닝을 위한 프롬프트 구성 (예: 문장 다듬기)
      	    String refinePrompt = promptProvider.getRefinerPrompt(page, page.getPreviousContext(), character, master.getSideCharacterAppearance() );

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // ComfyUI 호출 및 Polling 로직을 이 안으로 이동
                    // (주의: RestTemplate은 Thread-Safe 하므로 공유 가능)
                	// 1. Ollama 정제
                    //String rawResponse = this.forceCloseJson(ollamaClient.generate(refinePrompt));
                    String rawResponse = ollamaClient.generate(refinePrompt);
                    
                 // 빈 값 방어 로직 추가
                    if (rawResponse == null || rawResponse.trim().isEmpty()) {
                        log.error("Page {}: Ollama response is empty", page.getPageNumber());
                        return;
                    }
                    
                    // JSON 변환 수행
                    String closedJson = svcUtil.forceCloseJson(rawResponse);
                    RefinedStoryResponse refined = svcUtil.parseRefinedResponse(closedJson);
 //                   RefinedStoryResponse refined = this.parseRefinedResponse(rawResponse);

                    log.info("closedJson:::{}", closedJson);
//                    log.info("refined:::{}", refined.toString());
                    log.info("setRefinedContent:::{}", refined.getRefined_content());
                    //log.info("setRefinedContent:::{}", refined.getImage_prompt());
                    
                    // 엔티티에 값 세팅
                    page.setRefinedContent(refined.getRefined_content());
                    page.setRefinedImagePrompt(refined.getImage_prompt()); // DB 필드명에 맞게 조절            
                    page.setImagePrompt(refined.getImage_prompt()); // DB 필드명에 맞게 조절            
                    // 3. 결과 업데이트 (StoryPage 엔티티에 refinedContent 필드가 있다고 가정)
                    // 만약 단순 텍스트만 온다면 그대로 저장, JSON이라면 파싱 로직 추가 필요
                    //page.setRefinedContent(rawResponse);
                    //master.getPages().add(page);
                    String imageUrl = svcUtil.generateAndPollImage(restTemplate, page , mood, character ); 
                    page.setImageUrl(imageUrl); // 객체에 직접 세팅
                    
                    log.info("페이지 {}번 정제 완료", page.getPageNumber());
                } catch (Exception e) {
                    log.error("이미지 생성 실패: ", e);
                }
            });
            futures.add(future);
            
            // 2. Ollama 호출
 
        }
    // ⭐ 중요: 모든 비동기 작업이 완료될 때까지 메인 스레드를 대기시킵니다.
       CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
       
       storyMasterRepository.save(master) ;
        // @Transactional 어노테이션 덕분에 루프 종료 시 변경 감지(Dirty Checking)로 자동 업데이트 됩니다.
    }    
    
    @Transactional
    private StoryMaster createStoryDraft(GenerateBookRequest request, CharacterDTO character, int pageCount) {
        // 1. 프롬프트 빌드 (Resource에서 읽어온 템플릿 사용)
        String userChoices =  svcUtil.formatUserSelections(request.getSelections());
        String systemPrompt = promptProvider.getGeneratorPrompt(character, pageCount);
        String responseJson = ollamaClient.generate(systemPrompt + "\n\n" + userChoices );
        //String responseJson = ollamaClient.generate(systemPrompt  );
        
        try {
            // Ollama 응답에서 실제 JSON 부분만 추출하는 방어 로직 (LLM 특성상 설명 문구가 섞일 수 있음)
            String jsonOnly = responseJson.substring(responseJson.indexOf("{"), responseJson.lastIndexOf("}") + 1);
            
            log.info("jsonOnly::: {}", jsonOnly);
            // JSON -> DTO 변환
            RawStoryResponse rawStory = objectMapper.readValue(jsonOnly, RawStoryResponse.class);
            
            // 4. StoryMaster & StoryPage 생성 및 저장
            StoryMaster master = new StoryMaster();
            master.setTitle(rawStory.getTitle());
            master.setTotalPages(pageCount);
            master.setStatus(StoryStatus.RAW_READY);
            master.setSideCharacterAppearance(rawStory.getSide_character_description());
            
            // characterSetting 스냅샷 저장 로직 추가...
            List<StoryPage> pages = new ArrayList();  
            
            for (RawPageResponse p : rawStory.getPages()) {
                StoryPage page = new StoryPage();
                page.setStory(master);
                page.setPageNumber(p.getPageNumber()); 
                page.setPhase(p.getPhase()); 
                page.setRawContent(p.getContent()); 
                page.setRefinedContent(p.getContent()); 
                page.setRawImageKeywords(p.getRaw_image_keywords());
                page.setImagePrompt(p.getImage_prompt());
                pages.add(page);
            }

            master.setPages(pages);
            
            storyMasterRepository.save(master) ;
            
            return master;
            
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 중 오류가 발생했습니다.", e);
        }
    }
    
    @Transactional
    public UserStory createStory(StoryRequestDTO dto) {
        UserStory story = UserStory.builder()
                .userId(dto.getUserId())
                .storyId(dto.getStoryId())
                .title(dto.getTitle())
                .mainCharacter(dto.getMainCharacter())
                .selectedCodes(dto.getSelectedCodes()) // JSON 문자열로 전달 가정
                .status("COMPLETED")
                .build();

        return userStoryRepository.save(story);
    }    
   
    
}
