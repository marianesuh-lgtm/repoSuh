package com.mrs.shakes.service;

import org.springframework.stereotype.Service;

import com.mrs.shakes.domain.character.CharacterPrompt;
import com.mrs.shakes.domain.character.CharacterRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromptCompositeService {

    private final CharacterRepository characterRepository;

    /**
     * Ollama에게 보낼 '동화 생성 요청용' 시스템 프롬프트를 조립합니다.
     */
    public String buildOllamaSystemPrompt(String charId) {
        CharacterPrompt character = characterRepository.findById(charId)
                .orElseThrow(() -> new RuntimeException("캐릭터 정보가 없습니다."));

        return String.format(
            "당신은 동화 작가입니다. 주인공의 외모는 '%s'입니다. " +
            "이 특징을 유지하면서 아이들을 위한 짧은 동화를 써주세요. " +
            "각 페이지마다 이미지 생성을 위한 영어 묘사(Visual Action)를 'Visual: [영어 묘사]' 형식으로 포함해주세요.",
            character.getAppearance()
        );
    }
}
