package com.mrs.shakes.service;

import org.springframework.stereotype.Service;

import com.mrs.shakes.domain.character.CharacterPrompt;
import com.mrs.shakes.domain.character.CharacterRepository;
import com.mrs.shakes.dto.CharacterDTO;
import com.mrs.shakes.mapper.CharacterMapper;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class CharacterService {

    private final CharacterRepository characterRepository;
    private final CharacterMapper mapper;

    public CharacterPrompt getCharacterInfo(String id) {
        // 데이터가 없을 경우를 대비해 예외 처리를 함께 해주는 것이 관례입니다.
        return characterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 캐릭터가 DB에 없습니다: " + id));
    }
    
    public CharacterDTO getCharacterMapperInfo(String id) {
        // 데이터가 없을 경우를 대비해 예외 처리를 함께 해주는 것이 관례입니다.
        return mapper.retrieveCharacter(id);
    }

    public CharacterDTO getSubCharacterMapperInfo(String id) {
        // 데이터가 없을 경우를 대비해 예외 처리를 함께 해주는 것이 관례입니다.
        return mapper.retrieveSubCharacter(id);
    }
    
}
