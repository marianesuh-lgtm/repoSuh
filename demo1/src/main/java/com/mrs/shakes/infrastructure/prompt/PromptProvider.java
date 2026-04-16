package com.mrs.shakes.infrastructure.prompt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import com.mrs.shakes.domain.story.StoryPage;
import com.mrs.shakes.dto.CharacterDTO;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;



@Component
public class PromptProvider {

    private final ResourceLoader resourceLoader;

    public PromptProvider(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 1차 스토리 생성용 프롬프트 빌드
     */
    public String getGeneratorPrompt333(CharacterDTO character, int pageCount) {
        String template = loadTemplate("story-generator-v2.st");
        
        // 템플릿 내의 {variable} 들을 실제 값으로 치환
        return template;
    }
   
    /**
     * 1차 스토리 생성용 프롬프트 빌드
     */
    public String getGeneratorPrompt(CharacterDTO character, int pageCount) {
        String template = loadTemplate("story-generator-v1.st");
        
        // 템플릿 내의 {variable} 들을 실제 값으로 치환
        return template
            .replace("{appearance}", character.getAppearance())
            .replace("{personality}", character.getPersonalityTraits())
            .replace("{artStyle}", character.getArtStyle())
            .replace("{negative}", character.getNegative())
            .replace("{subTitle}", character.getSubTitle())
            .replace("{subAppearance}", character.getSubAppearance())
            .replace("{subCharacteristic}", character.getSubCharacteristic())
            .replace("{pageCount}", String.valueOf(pageCount));
    }

    /**
     * 2차 교정용(Refiner) 프롬프트 빌드
     */
     public String getRefinerPrompt(StoryPage page, String context, CharacterDTO character, String sideChar) {
        String template = loadTemplate("story-refiner-v2.st");
 
     // context가 null이면 빈 문자열로 치환하거나 기본 메시지를 넣습니다.
        String safeContext = (context == null || context.isEmpty())?  
                             "이것은 이야기의 시작입니다. 이전 줄거리가 없습니다." : context;        
        return template
            .replace("{phase}", page.getPhase())
            .replace("{previousContext}",  safeContext)
            .replace("{side_character_description}",  sideChar)
            .replace("{content}", page.getRawContent())
            .replace("{raw_image_description}", page.getRawImageKeywords() )
            .replace("{appearance}", character.getAppearance());
    }   
     
    private String loadTemplate(String fileName) {
        try {
            Resource resource = resourceLoader.getResource("classpath:prompts/" + fileName);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("프롬프트 템플릿 파일을 찾을 수 없습니다: " + fileName, e);
        }
    }
}