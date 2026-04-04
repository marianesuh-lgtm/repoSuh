package com.mrs.shakes.service;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

import com.mrs.shakes.config.ShakesProperties;
import com.mrs.shakes.dto.CharacterDTO;
import com.mrs.shakes.infrastructure.prompt.PromptProvider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
// 만약 Ollama 전용 옵션을 쓰신다면 아래도 필요합니다.


@Service
@RequiredArgsConstructor
public class IncrementalStoryService {

    private final ChatModel chatModel;
    private final ShakesProperties properties; // 이전 단계에서 만든 설정 클래스
    private final PromptProvider promptProvider; // .st 파일을 읽어주는 컴포넌트

    // 1. 결과물을 담을 DTO (내부 클래스)
    @Getter @ToString
    public static class FairyTalePage {
        private int pageNumber;
        private String content; // 동화 본문
        private String rawImageKeywords; // 이미지 생성용 키워드
        private String summaryForContext; // 다음 페이지 생성을 위한 요약 (숨김 처리 가능)

        // Ollama의 JSON 응답을 파싱하여 객체로 만드는 생성자 (가상의 파싱 로직)
        public FairyTalePage(int pageNumber, String ollamaJsonResponse) {
            this.pageNumber = pageNumber;
            // TODO: Jackson 등을 이용해 ollamaJsonResponse를 실제 필드에 매핑
            // 예: this.content = json.get("content").asText();
        }
    }

    public List<FairyTalePage> generateStoryRefining(String userTopic, int totalPages) {
        List<FairyTalePage> finalBook = new ArrayList<>();
        String previousContext = ""; // 핵심: 이전 페이지들의 누적 줄거리

        OllamaChatOptions options = OllamaChatOptions.builder()
                .model(properties.getOllama().getModel()) // gemma2
                .temperature(0.8) // 동화 작풍을 위해 창의성 부여
                .format("json") // JSON 응답 강제
                .build();
        //String phase, String rawContent, String rawKeywords, CharacterDTO character, String context
        for (int currentPage = 1; currentPage <= totalPages; currentPage++) {
            // 2. 페이지별 맞춤형 시스템 프롬프트 생성
//            String systemPrompt = promptProvider.getRefinerPrompt(currentPage, totalPages, previousContext);
            String systemPrompt =  createSystemPrompt(currentPage, totalPages, previousContext);
            String userPrompt = String.format("주제: %s. 이 주제로 %d페이지의 이야기를 지어주세요.", userTopic, currentPage);

            // 3. Ollama 호출
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(systemPrompt),
                    new UserMessage(userPrompt)
            ), options);

            ChatResponse response = chatModel.call(prompt);
            String jsonResponse = response.getResult().getOutput().getText(); 

            // 4. 결과 파싱 및 누적
            FairyTalePage generatedPage = new FairyTalePage(currentPage, jsonResponse);
            finalBook.add(generatedPage);

            // 5. 핵심: 다음 페이지를 위한 컨텍스트 갱신
            // gemma2가 응답한 JSON에 'summaryForContext' 필드를 포함하도록 프롬프트에 명시해야 합니다.
            previousContext += generatedPage.getSummaryForContext() + " "; 
        }

        return finalBook;
    }

    // 6. 페이지 단위 생성을 위한 정교한 시스템 프롬프트 함수
    private String createSystemPrompt(int currentPage, int totalPages, String previousContext) {
        String phase = getPhase(currentPage, totalPages); // 기-승-전-결 판단

        return String.format("""
            당신은 전 세계 아이들을 사로잡는 위대한 동화 작가 'Mrs. Shakespeare'입니다.
            제시된 주제를 바탕으로, 오직 **%d페이지**에 해당하는 이야기만 지어주세요.

            [현재 작업 정보]
            - 총 페이지: %d / 현재 페이지: %d
            - 이야기의 단계: %s (이 단계에 맞는 극적 전개를 해주세요.)

            [이전까지의 줄거리 (Context)]
            %s
            (중요: 이 줄거리와 자연스럽게 이어지도록 서술하되, 주인공의 성격이나 외모 설정이 바뀌지 않도록 절대적으로 주의하세요.)

            [출력 형식]
            반드시 아래 구조의 순수한 JSON 형식으로만 응답하세요. 다른 설명은 필요 없습니다.
            {
              "content": "아이들이 읽기 좋은 따뜻하고 생동감 넘치는 동화 본문 (한국어)",
              "raw_image_keywords": "이 페이지의 장면을 그리기 위한 구체적인 영문 키워드 (예: 'princess with silver wings, smiling, colorful flower garden')",
              "summaryForContext": "다음 페이지 작가가 참고할 수 있도록, 이 페이지의 핵심 사건을 1줄로 요약한 것 (한국어)"
            }
            """, currentPage, totalPages, currentPage, phase, previousContext.isEmpty() ? "(처음 시작)" : previousContext);
    }

    // 페이지 번호에 따라 기승전결(Phase)을 판단하는 간단한 로직
    private String getPhase(int currentPage, int totalPages) {
        float progress = (float) currentPage / totalPages;
        if (progress <= 0.25) return "기 (도입, 배경 및 캐릭터 소개)";
        if (progress <= 0.50) return "승 (전개, 사건의 시작)";
        if (progress <= 0.75) return "전 (위기 및 갈등 심화)";
        return "결 (결말 및 교훈)";
    }
}