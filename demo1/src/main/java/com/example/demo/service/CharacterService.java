package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.domain.character.CharacterPrompt;
import com.example.demo.domain.character.CharacterRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;

    public CharacterPrompt getCharacterInfo(String id) {
        // 데이터가 없을 경우를 대비해 예외 처리를 함께 해주는 것이 관례입니다.
        return characterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 캐릭터가 DB에 없습니다: " + id));
    }
}
