package com.mrs.shakes.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrs.shakes.domain.character.CharacterPrompt;
import com.mrs.shakes.domain.character.CharacterRepository;

import lombok.RequiredArgsConstructor;

@RestController // 확인!
@RequiredArgsConstructor
@RequestMapping("/api/characters") // 확인!
public class CharacterCheckController {
	
	final CharacterRepository characterRepository ;

   
    @GetMapping // 이 경우 최종 주소는 http://localhost:8080/api/characters
    public ResponseEntity<List<CharacterPrompt>> getAllCharacters() {
        List<CharacterPrompt> characters = characterRepository.findAll();

        // 데이터가 하나도 없는 경우 (204 No Content 반환 선택 가능)
        if (characters.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // 데이터가 있는 경우 200 OK와 함께 리스트 반환
        return ResponseEntity.ok(characters);
    }
}
