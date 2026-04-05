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
    public String getGeneratorPrompt(CharacterDTO character, int pageCount) {
        String template = loadTemplate("story-generator-v1.st");
        
        // 템플릿 내의 {variable} 들을 실제 값으로 치환
        return template
            .replace("{appearance}", character.getAppearance())
            .replace("{weakness}", character.getWeakness())
            .replace("{personality}", character.getPersonalityTraits())
            .replace("{artStyle}", character.getArtStyle())
            .replace("{negative}", character.getNegative())
            .replace("{subTitle}", character.getSubTitle())
            .replace("{subAppearance}", character.getSubAppearance())
            .replace("{pageCount}", String.valueOf(pageCount));
    }

    /**
     * 2차 교정용(Refiner) 프롬프트 빌드
     */
     public String getRefinerPrompt(StoryPage page, String context, CharacterDTO character) {
        String template = loadTemplate("story-refiner-v1.st");
        
        return template
            .replace("{phase}", page.getPhase())
            .replace("{previousContext}",  context.isEmpty() ? "이야기 시작 단계입니다." : context)
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