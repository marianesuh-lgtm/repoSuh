package com.mrs.shakes.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mrs.shakes.domain.character.CharacterPrompt;
import com.mrs.shakes.domain.character.CharacterRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(CharacterRepository repository) {
        return args -> {
            // 이미 데이터가 있다면 추가로 넣지 않도록 방어 로직 추가
            if (repository.count() == 0) {
                // Alice 캐릭터 생성 및 저장
                CharacterPrompt alice = CharacterPrompt.builder()
                        .charId("HERO_ALICE")
                        .appearance("blonde hair, blue dress, signature ribbon")
                        .artStyle("watercolor storybook style, soft lighting")
                        .negative("animal ears, fox ears, extra fingers")
                        .build();

                // 수리공 캐릭터 생성 및 저장
                CharacterPrompt fixer = CharacterPrompt.builder()
                        .charId("FIXER_KIM")
                        .appearance("middle-aged man, gray jumpsuit, holding a wrench")
                        .artStyle("cinematic thriller lighting, detailed textures")
                        .negative("cartoonish, bright colors")
                        .build();

                repository.save(alice);
                repository.save(fixer);
                
                System.out.println(">>> 초기 캐릭터 데이터가 DB에 저장되었습니다.");
            }
        };
    }
}
