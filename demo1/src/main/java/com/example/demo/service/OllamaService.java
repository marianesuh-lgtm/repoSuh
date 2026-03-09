package com.example.demo.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

import com.example.demo.client.OllamaClient;
import com.example.demo.dto.GenerateBookRequest;
import com.example.demo.dto.GenerateBookRequest.StorySelections;
import com.example.demo.dto.PagedStoryResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;  // 옵션 빌더용
import org.springframework.ai.chat.messages.UserMessage;   // 필요 시
import org.springframework.ai.chat.messages.SystemMessage; // 필요 시


@Service
@Slf4j
@RequiredArgsConstructor
public class OllamaService {

    private final OllamaClient ollamaClient;  // Ollama API 호출용 클라이언트 (RestTemplate, WebClient 등으로 구현)
    private final OllamaChatModel ollamaChatModel;
    
    public PagedStoryResponse generatePagedStory(GenerateBookRequest request) {
    	
    	log.info("generatePagedStory  request:: {}", request);
        // 1. 사용자 선택지 → 자연스러운 프롬프트로 변환
        String userChoices =  formatUserSelections(request.getSelections());

        String systemPrompt = buildSystemPrompt(request.getPageCount());
        String fullPrompt = systemPrompt + "\n\n" + userChoices;

    	log.info("generatePagedStory  userChoices:: {}", userChoices);
    	log.info("generatePagedStory  systemPrompt:: {}", systemPrompt);
    	log.info("generatePagedStory  fullPrompt:: {}", fullPrompt);
       
        //String fullPrompt = buildSystemPrompt(request.getPageCount()) + "\n\n" + formatUserSelections(request.getSelections());

        // Spring AI ChatModel로 호출 (가장 간단한 방식)
//        Prompt prompt = new Prompt(fullPrompt, OllamaChatOptions.builder()
//                .model("qwen-story")  // 또는 application.yml에서 설정
//                .temperature(request.getTemperature() != null ? request.getTemperature() : 0.75f)
//                .build());
        Prompt prompt = new Prompt(fullPrompt);
        
        ChatResponse response = ollamaChatModel.call(prompt);
       	log.info("generatePagedStory  response:: {}", response);
       String generatedText = response.getResult().getOutput().getText();
        
    	log.info("generatePagedStory  generatedText:: {}", generatedText);
        
        // 4. 응답 파싱 → 제목 + 페이지별 텍스트 + 이미지 프롬프트 추출
        return parseToPagedResponse(generatedText, request.getPageCount());    }

    private String formatUserSelections(StorySelections selections) {
        if (selections == null) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("[선택된 이야기 요소]\n");

        if (selections.get기() != null) {
            sb.append("- 주인공: ").append(selections.get기().getCharacter()).append("\n");
            sb.append("- 장소: ").append(selections.get기().getPlace()).append("\n");
            sb.append("- 기분/상황: ").append(selections.get기().getMood()).append("\n");
        }
        if (selections.get승() != null) {
            sb.append("- 사건: ").append(selections.get승().getEvent()).append("\n");
            sb.append("- 동행: ").append(selections.get승().getCompanion()).append("\n");
        }
        if (selections.get전() != null) {
            sb.append("- 문제: ").append(selections.get전().getProblem()).append("\n");
            sb.append("- 시도: ").append(selections.get전().getTryAction()).append("\n");
        }
        if (selections.get결() != null) {
            sb.append("- 해결: ").append(selections.get결().getSolution()).append("\n");
            sb.append("- 엔딩: ").append(selections.get결().getEnding()).append("\n");
        }

        return sb.toString();
    }

    private String buildSystemPrompt(int pageCount) {
        return """
                당신은 4~10세 어린이를 위한 따뜻하고 귀여운 한국어 동화 작가입니다.
                아래 규칙을 반드시 지켜 정확한 형식으로 출력하세요.

                [규칙]
                1. 모든 답변은 100%% 순수 한국어 한글로만 작성합니다. 영어, 중국어, 한자 절대 사용 금지.
                2. 첫 줄에 반드시 "제목: [매우 귀엽고 창의적인 제목]" 형식으로 제목만 작성
                3. 정확히 %d페이지로 구성합니다.
                4. 각 페이지는 2~4문장으로 짧고 리듬감 있게 작성
                5. 의성어·의태어 적극 사용 (반짝반짝, 폴짝폴짝, 쿵쾅쿵쾅, 휙휙 등)
                6. 따뜻한 교훈이 자연스럽게 녹아들도록
                7. 각 페이지 끝에 반드시 아래 형식으로 이미지 프롬프트를 영어로 추가
                   이미지 프롬프트: cute, whimsical children's picture book illustration, vibrant colors, soft lighting, 16:9 aspect ratio, ...

                [출력 형식 - 절대 이 형식을 깨지 마세요]

                제목: 예쁜 제목 여기에

                페이지 1
                [2~4문장 한국어 이야기]

                이미지 프롬프트: cute magical cat ... whimsical style, 16:9

                페이지 2
                [다음 장면 이야기]

                이미지 프롬프트: ...

                (페이지 %d까지 계속)
                """.formatted(pageCount, pageCount);
    }

//    private String callOllama(String fullPrompt, GenerateBookRequest request) {
//        try {
//            OllamaApi ollamaAPI = new OllamaApi("http://localhost:11434");
//            ollamaAPI.setVerbose(true); // 디버깅용 로그 켜기
//
//            OllamaGenerateRequest ollamaReq = OllamaGenerateRequest.builder()
//                .model("gemma2:9b")  // 또는 요청에서 동적으로 받기
//                .prompt(fullPrompt)
//                .temperature(request.getTemperature() != null ? request.getTemperature() : 0.75)
//                .numPredict(request.getMaxTokens() != null ? request.getMaxTokens() : 1200)
//                .build();
//
//            OllamaResponse response = ollamaAPI.generate(ollamaReq);
//            return response.getResponse();  // 생성된 텍스트 반환
//
//        } catch (Exception e) {
//            log.error("Ollama 호출 실패", e);
//            return "";
//        }
//    }
    
    private PagedStoryResponse parseToPagedResponse(String rawResponse, int expectedPageCount) {
        List<String> lines = Arrays.stream(rawResponse.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        String title = "";
        List<PagedStoryResponse.Page> pages = new ArrayList<>();

        String currentText = "";
        String currentPrompt = "";
        int pageNum = 0;

        for (String line : lines) {
            if (line.startsWith("제목:")) {
                title = line.replace("제목:", "").trim();
                continue;
            }

            if (line.startsWith("페이지 ")) {
                // 이전 페이지 저장
                if (pageNum > 0 && !currentText.isEmpty()) {
                    pages.add(PagedStoryResponse.Page.builder()
                            .pageNumber(pageNum)
                            .text(currentText.trim())
                            .imagePrompt(currentPrompt.trim())
                            .build());
                }
                pageNum++;
                currentText = "";
                currentPrompt = "";
                continue;
            }

            if (line.startsWith("이미지 프롬프트:")) {
                currentPrompt = line.replace("이미지 프롬프트:", "").trim();
            } else if (!line.isEmpty()) {
                currentText += line + "\n";
            }
        }

        // 마지막 페이지 저장
        if (pageNum > 0 && !currentText.isEmpty()) {
            pages.add(PagedStoryResponse.Page.builder()
                    .pageNumber(pageNum)
                    .text(currentText.trim())
                    .imagePrompt(currentPrompt.trim())
                    .build());
        }

        // 페이지 수가 맞지 않으면 로그 남기기
        if (pages.size() != expectedPageCount) {
            log.warn("요청한 페이지 수({})와 실제 생성 페이지 수({})가 다릅니다", expectedPageCount, pages.size());
        }

        return PagedStoryResponse.builder()
                .title(title)
                .pages(pages)
                .build();
    }
}
