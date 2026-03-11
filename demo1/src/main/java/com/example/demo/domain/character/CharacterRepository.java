package com.example.demo.domain.character;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterRepository extends JpaRepository<CharacterPrompt, String> {

	// 1. 캐릭터 이름(필드명)으로 찾고 싶을 때
    // Optional<CharacterPrompt> findByCharName(String name);

    // 2. 특정 화풍(artStyle)을 가진 캐릭터들만 모아서 보고 싶을 때
    List<CharacterPrompt> findAllByArtStyle(String artStyle);

    // 3. 특정 단어가 포함된 외모 묘사를 가진 캐릭터 찾기 (검색 기능)
    List<CharacterPrompt> findByAppearanceContaining(String keyword);
}
