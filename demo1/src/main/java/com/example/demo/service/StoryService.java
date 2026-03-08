package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.client.ComfyUiClient;
import com.example.demo.client.OllamaClient;
import com.example.demo.controller.BookStoryController;
import com.example.demo.dto.NextSceneRequest;
import com.example.demo.dto.NextSceneResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor  // Lombok 사용 시 이 어노테이션 하나로 생성자 자동 생성
@Slf4j

public class StoryService {

    private final OllamaClient ollamaClient;  // Ollama API 호출 클라이언트
    private final ComfyUiClient comfyUiClient; // 이미지 생성 클라이언트

 public NextSceneResponse generateNextScene(NextSceneRequest req) {
    	log.info("request:: {}", req);
    	        // 1. 프롬프트 템플릿으로 다음 장면 생성 요청
        String prompt = this.buildNextScenePrompt(req);
    	log.info("29  prompt:: {}", prompt);
        
        String nextPart = ollamaClient.generate(prompt, req.getMaxLength());
        
    	log.info("prompt:: {}", prompt);
    	log.info("nextPart:: {}", nextPart);

        // 2. 이미지 프롬프트 생성
        String imagePrompt = generateImagePrompt(req.getChildName() , req.getAge() , req.getGender() , nextPart, req.getTheme() );
    	log.info("imagePrompt:: {}", imagePrompt);

        // 3. 이미지 생성 요청 (비동기면 큐에 넣고 즉시 URL 반환 가능)
        String imageUrl = comfyUiClient.generateImage(imagePrompt); // 동기면 여기서 대기
    	log.info("imageUrl:: {}", imageUrl);

        // 4. 다음 선택지 3개 생성
        List<String> nextChoices = generateChoices(nextPart, req.getStep() );
    	log.info("nextChoices:: {}", nextChoices);
        
        NextSceneResponse rtnRes = new NextSceneResponse ();
        rtnRes.setSuccess(true); 
        rtnRes.setMessage(null);
        rtnRes.setNextPart(nextPart);
        rtnRes.setImagePrompt(imagePrompt);
        rtnRes.setImageUrl(imageUrl);
        rtnRes.setNextChoices(nextChoices);
        rtnRes.setCurrentStep(req.getStep() + 1);
        

        return rtnRes;
    }

    private String buildNextScenePrompt(NextSceneRequest req) {
    	log.info("62  req:: {}", req);
        String prompt33 = """
        	You are a professional English prompt writer for image generation.\\	
           	사용자가 한국어로 설명해도 당신은 반드시 다음 규칙을 철저히 지켜야 해:\
            모든 이야기의 주인공은 반드시 '%s'라는 이름의 %d살 %s 아이야.\
            너는 아이와 함께 동화를 만드는 친절한 AI야.\
            지금까지 이야기: \
            %s \
             '%s'(%d살, 성별은 %s, 좋아하는 것: %s)가 방금 선택한 행동: %s \
            이제 다음 장면을 150자 이내로 재미있고 따뜻하게 이어써줘.\
            기승전결을 고려해서 자연스럽게 전개해.\
            현재 단계: %d (1:기, 2:승, 3:전, 4:결)\
            형식: 페이지1: [150자 이내로 재미있고 따뜻하게 이어써줘] [영어로 된 간단한 이미지 설명 프롬프트]\
            """.formatted(
            req.getChildName(),
            req.getAge(),
            req.getGender() ,
            req.getCurrentStory() ,
            req.getChildName(),
            req.getAge(),
            req.getGender() ,
            req.getLikes() ,
            req.getUserChoice() ,
            req.getStep() 
        );
        
        String prompt = """
            	You are a professional English prompt writer for image generation.	
            	사용자가 한국어로 설명해도 당신은 반드시 다음 규칙을 철저히 지켜야 해:
                모든 이야기의 주인공은 반드시 '%s'라는 이름의 %d살 아이야.
                 항상 '%s'라고 이름으로 부르고 %d살 나이에 맞게 테마를 만들어줘.
                이야기 전체에서 '%s'를 주인공 이름으로 사용해.

                좋아하는 것: %s
                테마: %s
                
                8페이지 분량의 귀여운 한국어 동화 스토리를 작성해줘.
                형식: 페이지1: [짧은 문장 1~2개] [영어로 된 간단한 이미지 설명 프롬프트]
                페이지2: ...
                """.formatted(req.getChildName(), req.getAge(), req.getChildName(), req.getAge(), req.getChildName(),  req.getLikes(), req.getTheme());

        log.info("buildNextScenePrompt prompt::: {}", prompt);
      //log.info("buildNextScenePrompt prompt22::: {}", prompt22);
        
        return prompt;
    }

    private String generateImagePrompt(String name, int age, String gender, String text, String theme) {
        String prompt = """
          cute children's book illustration, vibrant colors, soft lighting, \" +
               age + "-year-old Korean " + gender + " child named " + name + ", " + text + ", " +
               "theme: " + theme + ", whimsical, heartwarming, detailed background 
        		""";
        log.info("generateImagePrompt prompt::: {}", prompt);
       return prompt;
    }

    private List<String> generateChoices(String nextPart, int step) {
        // Ollama나 간단한 규칙으로 3개 선택지 생성
        // 예: Ollama에 "다음 장면에서 아이가 할 수 있는 선택지 3개를 제안해" 요청
        return List.of("A 선택지", "B 선택지", "C 선택지");
    }
}