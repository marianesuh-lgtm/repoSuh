package com.example.demo.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

import com.example.demo.client.OllamaClient;
import com.example.demo.domain.character.CharacterPrompt;
import com.example.demo.domain.character.CharacterRepository;
import com.example.demo.dto.CharacterDTO;
import com.example.demo.dto.GenerateBookRequest;
import com.example.demo.dto.PagedStoryResponse;
import com.example.demo.dto.GenerateBookRequest.StorySelections;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor

public class OllamaTestService {

	
    private final OllamaClient ollamaClient;  // Ollama API 호출용 클라이언트 (RestTemplate, WebClient 등으로 구현)
    private final OllamaChatModel ollamaChatModel;
    private final CharacterRepository characterRepository;
    private final CharacterService characterService ;
    
    public PagedStoryResponse generatePagedStory(GenerateBookRequest request) {
    	
    	log.info("generatePagedStory  request:: {}", request);
        // 1. 사용자 선택지 → 자연스러운 프롬프트로 변환
        String userChoices =  formatUserSelections(request.getSelections());

        String charId = request.getSelections().get기().getCharacter().getCode();
        
     // 1. DB에서 캐릭터 정보 조회
//        CharacterPrompt character = characterRepository.findById(charId)
//                .orElseThrow(() -> new RuntimeException("캐릭터 정보를 찾을 수 없습니다: " + charId));
        CharacterDTO character = characterService.getCharacterMapperInfo(charId);
        character.setBackground(request.getSelections().get기().getPlace().getLabel());
        character.setMood(request.getSelections().get기().getMood().getLabel());
        character.setProblem(request.getSelections().get전().getProblem().getLabel());
        
        log.info("character:: {}",character);
        int tempCnt = request.getPageCount();
            //tempCnt = 3;
        
        //String systemPrompt = buildSystemPrompt(character, request.getPageCount());
        String systemPrompt = buildSystemPrompt(character, tempCnt);
        String fullPrompt = systemPrompt + "\n\n" + userChoices;

    	log.info("generatePagedStory  userChoices:: {}", userChoices);
    	log.info("generatePagedStory  systemPrompt:: {}", systemPrompt);
    	log.info("generatePagedStory  fullPrompt:: {}", fullPrompt);
       
        //String fullPrompt = buildSystemPrompt(request.getPageCount()) + "\n\n" + formatUserSelections(request.getSelections());

        // Spring AI ChatModel로 호출 (가장 간단한 방식)
//        Prompt prompt = new Prompt(fullPrompt, OllamaChatOptions.builder()
//                .model("my-eeve")  // 또는 application.yml에서 설정
//                .temperature(request.getTemperature() != null ? request.getTemperature() : 0.75f)
//                .build());
        Prompt prompt = new Prompt(fullPrompt);
        
       	log.info("generatePagedStory before ollamaChatModel.call(prompt)...  prompt:: {}", prompt);
       	
       ChatResponse response = ollamaChatModel.call(prompt);
       	log.info("generatePagedStory  response:: {}", response);
       String generatedText = response.getResult().getOutput().getText();
        
    	log.info("generatePagedStory  generatedText:: {}", generatedText);
        
        // 4. 응답 파싱 → 제목 + 페이지별 텍스트 + 이미지 프롬프트 추출
        //return parseToPagedResponse(generatedText, request.getPageCount());    }
        return parseToPagedResponse(generatedText, tempCnt);    }

    private String formatUserSelections(StorySelections selections) {
        if (selections == null) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("[선택된 이야기 요소]\n");

        if (selections.get기() != null) {
            sb.append("- 주인공: ").append(selections.get기().getCharacter().getLabel()).append("\n");
            sb.append("- 장소: ").append(selections.get기().getPlace().getLabel()).append("\n");
            sb.append("- 기분/상황: ").append(selections.get기().getMood().getLabel()).append("\n");
        }
        if (selections.get승() != null) {
            sb.append("- 사건: ").append(selections.get승().getEvent().getLabel()).append("\n");
            sb.append("- 동행: ").append(selections.get승().getCompanion().getLabel()).append("\n");
        }
        if (selections.get전() != null) {
            sb.append("- 문제: ").append(selections.get전().getProblem().getLabel()).append("\n");
            sb.append("- 시도: ").append(selections.get전().getTryAction().getLabel()).append("\n");
        }
        if (selections.get결() != null) {
            sb.append("- 해결: ").append(selections.get결().getSolution().getLabel()).append("\n");
            sb.append("- 엔딩: ").append(selections.get결().getEnding().getLabel()).append("\n");
        }

        return sb.toString();
    }

    /**
     * 캐릭터 정보를 동적으로 주입하는 시스템 프롬프트 생성기
     */
    private String buildSystemPrompt(CharacterDTO character, int pageCount) {
        return """
                당신은 4~7세 어린이를 위한 따뜻하고 귀여운 한국어 동화 작가입니다.
                
                [주인공 설정]
                - 외모: %s
                - 성격: %s
                - 화풍: %s
                - 금지 요소: %s
                
                아래 규칙대로 반드시 지켜 정확한 형식으로 출력하세요.

                [규칙]
                1. 모든 답변은 100%% 순수 한국어 한글로만 작성합니다. (이미지 프롬프트 제외)
                2. 첫 줄에 반드시 "제목: [매우 귀엽고 창의적인 제목]" 형식으로 제목만 작성
                3. 정확히 %d페이지로 구성합니다.
                4.  각 페이지는 2~3문장으로 짧고 리듬감 있게 작성합니다.
                5. 각 페이지 하단 에는 아래 규칙을 지켜서 영어 프롬프트를 만들어줘.
				  5.1. background: %s, 조명, 배경 디테일
				  5.2. action: 인물의 손동작, 화면을 보는 눈빛
				  5.3. mood: %s

                 반드시 항목별로 구분해서 영문 키워드 위주로 리턴해줘.
                   
                [출력 형식]
                제목: 예쁜 제목 여기에

                [PAGE_START]
				NUMBER: 1
				CONTENT: (이야기 내용, 따옴표는 ' 사용)
				BG: (영문 배경 키워드)
				ACTION: (영문 행동 키워드)
				MOOD: (영문 분위기 키워드)
				[PAGE_END]
                
                (페이지 %d까지 반복)
                """.formatted(
                    character.getAppearance(), 
                    character.getPersonalityTraits(), 
                    character.getArtStyle(),
                    character.getNegative(),
                    pageCount, 
                    character.getBackground(),
                    character.getMood(),
//                    character.getAppearance()+','+character.getPersonalityTraits(),
//                    character.getAppearance(), // 이미지 프롬프트 예시용
//                    character.getNegative(),   // 이미지 프롬프트 예시용
                    pageCount
                );
    }
    
    
    private PagedStoryResponse parseToPagedResponse(String rawResponse, int expectedPageCount) {
        String[] lines = rawResponse.split("\n");
        List<PagedStoryResponse.Page> pages = new ArrayList<>();
        
        int pageNum = 0;
        String currentText = "", currentBg = "", currentAction = "", currentMood = "";
        String title = "무제";

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty() || line.equals("[PAGE_END]")) continue;


            // 1. 제목 처리 (## 체크보다 먼저 수행)
            if (line.contains("제목:")) {
                title = line.replace("##", "").replace("제목:", "").replace("*", "").trim();
                log.info("추출된 제목: {}", title);
                continue;
            }
 
            // 제목 외의 불필요한 헤더나 구분선 무시
            if (line.startsWith("##")) continue;

            
            // 1. 페이지 시작 인식 ([PAGE_START] 만 체크)
            if (line.contains("[PAGE_START]")) {
                if (pageNum > 0) {
                    pages.add(buildPage(pageNum, currentText.trim(), currentBg, currentAction, currentMood));
                }
                pageNum++;
                currentText = ""; currentBg = ""; currentAction = ""; currentMood = "";
                continue;
            }

            // 2. 키워드별 매칭 및 데이터 추출
            String lowerLine = line.toLowerCase();
            if (lowerLine.contains("content:")) {
                currentText = extractValue(line, "CONTENT:");
            } else if (lowerLine.contains("bg:") || lowerLine.contains("background:")) {
                currentBg = extractValue(line, lowerLine.contains("bg:") ? "BG:" : "background:");
            } else if (lowerLine.contains("action:")) {
                currentAction = extractValue(line, "ACTION:");
            } else if (lowerLine.contains("mood:")) {
                currentMood = extractValue(line, "MOOD:");
            } else if (lowerLine.contains("number:")) {
                // NUMBER 라인은 페이지 번호 동기화용으로 사용하거나 무시
                continue;
            } else if (pageNum > 0 && !line.startsWith("[")) {
                // 키워드가 없지만 페이지 내부인 경우 텍스트 추가 (줄바꿈 대응)
                currentText += " " + line;
            }
        }
        
        // 마지막 페이지 추가
        if (pageNum > 0) {
            pages.add(buildPage(pageNum, currentText.trim(), currentBg, currentAction, currentMood));
        }

        log.info("최종 파싱된 페이지 수: {}", pages.size());
        return PagedStoryResponse.builder().title(title).pages(pages).build();
    }

    // 추출 유틸리티 (대소문자 구분 없이 키워드 이후 값만 가져오기)
    private String extractValue(String line, String keyword) {
        int index = line.toUpperCase().indexOf(keyword.toUpperCase());
        if (index != -1) {
            return line.substring(index + keyword.length()).trim();
        }
        return "";
    }

    private PagedStoryResponse parseToPagedResponse99(String rawResponse, int expectedPageCount) {
        // 1. 모든 줄바꿈과 공백 정돈
        String[] lines = rawResponse.split("\n");
        List<PagedStoryResponse.Page> pages = new ArrayList<>();
        
        int pageNum = 0;
        String currentText = "", currentBg = "", currentAction = "", currentMood = "";

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // 페이지 인식 (숫자가 포함된 '페이지' 라인 찾기)
            if (line.contains("페이지") || line.contains("PAGE")) {
                if (pageNum > 0) {
                    pages.add(buildPage(pageNum, currentText, currentBg, currentAction, currentMood));
                }
                pageNum++;
                currentText = ""; currentBg = ""; currentAction = ""; currentMood = "";
                continue;
            }

            // 키워드별 매칭 (Gemma2는 콜론 앞뒤에 별표가 있을 수 있음)
            if (line.toLowerCase().contains("background:")) {
                currentBg = extractValue(line, "background:");
            } else if (line.toLowerCase().contains("action:")) {
                currentAction = extractValue(line, "action:");
            } else if (line.toLowerCase().contains("mood:")) {
                currentMood = extractValue(line, "mood:");
            } else if (!line.contains(":") && pageNum > 0) {
                // 키워드가 없는 일반 문장은 이야기 내용으로 간주
                currentText += line + " ";
            }
        }
//        log.info("parseToPagedResponse pageNum:: {}", pageNum);
//        log.info("parseToPagedResponse currentText:: {}", currentText);
//        log.info("parseToPagedResponse currentBackground:: {}", currentBg);
//        log.info("parseToPagedResponse currentMood:: {}", currentMood);
//        log.info("parseToPagedResponse currentAction:: {}", currentAction);
        
        // 마지막 페이지 추가
        if (pageNum > 0) {
            pages.add(buildPage(pageNum, currentText, currentBg, currentAction, currentMood));
        }

        log.info("parseToPagedResponse pages:: {}", pages);
        return PagedStoryResponse.builder().pages(pages).build();
    }

    private PagedStoryResponse.Page buildPage(int num, String text, String bg, String action, String mood) {
        // 1. 이미지 생성용 통합 프롬프트 생성 (배경, 행동, 분위기 조합)
        // 한글이 섞여 들어올 경우를 대비해 기본 키워드를 추가하거나 정제할 수 있습니다.
        String combinedPrompt = String.format("%s, %s, %s", 
                cleanText(bg), 
                cleanText(action), 
                cleanText(mood));

        // 2. 동화 내용(text)에서 JSON 구조를 깨뜨릴 수 있는 쌍따옴표 등을 단일 따옴표로 변환
        String safeText = text.replace("\"", "'").replace("\n", " ").trim();

//        log.info("buildPage combinedPrompt:: {}", combinedPrompt);
//        log.info("buildPage safeText:: {}", safeText);
//        log.info("buildPage return PagedStoryResponse:: {}", 
//        		PagedStoryResponse.builder()
//                .title(title)
//                .Page.builder()
//                .pageNumber(num)
//                .title(title)
//                .text(safeText)
//                .background(cleanText(bg))
//                .action(cleanText(action))
//                .mood(cleanText(mood))
//                .imagePrompt(combinedPrompt) // 최종적으로 ComfyUI 등에 전달될 필드
//                .build());

        return PagedStoryResponse.Page.builder()
                .pageNumber(num)
                .text(safeText)
                .background(cleanText(bg))
                .action(cleanText(action))
                .mood(cleanText(mood))
                .imagePrompt(combinedPrompt) // 최종적으로 ComfyUI 등에 전달될 필드
                .build();
    }

    // 마크다운 기호나 불필요한 공백을 제거하는 헬퍼 메소드
    private String cleanText(String input) {
        if (input == null || input.isEmpty()) return "";
        return input.replace("*", "")  // 마크다운 강조 제거
                    .replace("#", "")  // 헤더 기호 제거
                    .replace("\"", "'") // 쌍따옴표 제거 (JSON 보호)
                    .trim();
    }    
    // 부가 정보 추출 헬퍼
    private String extractValue77(String line, String key) {
        String value = line.substring(line.toLowerCase().indexOf(key) + key.length()).trim();
        return value.replace("*", ""); // 마크다운 제거
    }    
    
    private PagedStoryResponse parseToPagedResponse333(String rawResponse, int expectedPageCount) {
        List<String> lines = Arrays.stream(rawResponse.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        String title = "무제";
        List<PagedStoryResponse.Page> pages = new ArrayList<>();

        String currentText = "";
        String currentBackground = "";
        String currentMood = "";
        String currentAction = "";
        int pageNum = 0;

        for (String line : lines) {
            // 1. 제목 처리
            if (line.contains("제목:")) {
                title = line.replace("제목:", "").replace("*", "").trim();
                continue;
            }

            // 2. 페이지 시작 처리 (데이터 형식: **페이지 1**)
            if (line.contains("페이지") && line.contains("**")) {
                // 이전 페이지가 있다면 리스트에 추가
                if (pageNum > 0) {
                    pages.add(createPage(pageNum, currentText, currentBackground, currentMood, currentAction));
                }
                
                pageNum++;
                // 변수 초기화
                currentText = "";
                currentBackground = "";
                currentMood = "";
                currentAction = "";
                continue;
            }

            // 3. 각 항목별 데이터 추출 (내용에 특정 키워드가 포함되었는지 확인)
            if (line.contains("이야기 내용:")) {
                // "이야기 내용:" 글자 자체는 제거
                currentText += line.replace("이야기 내용:", "").replace("**", "").trim() + "\n";
            } else if (line.toLowerCase().contains("background:")) {
                currentBackground = line.split(":", 2)[1].replace("*", "").trim();
            } else if (line.toLowerCase().contains("action:")) {
                currentAction = line.split(":", 2)[1].replace("*", "").trim();
            } else if (line.toLowerCase().contains("mood:")) {
                currentMood = line.split(":", 2)[1].replace("*", "").trim();
            } else if (line.startsWith("-") || line.contains("영어 프롬프트 요약")) {
                // 하단의 요약 부분은 무시하거나 별도 처리
                continue;
            } else {
                // 태그가 없는 일반 문장은 이야기 내용에 추가
                currentText += line.replace("**", "").trim() + "\n";
            }
        }
        
        log.info("pageNum:: {}", pageNum);
        log.info("currentText:: {}", currentText);
        log.info("currentBackground:: {}", currentBackground);
        log.info("currentMood:: {}", currentMood);
        log.info("currentAction:: {}", currentAction);

        // 마지막 페이지 저장
        if (pageNum > 0) {
            pages.add(createPage(pageNum, currentText, currentBackground, currentMood, currentAction));
        }

        log.info("pages:: {}", pages);
        
        return PagedStoryResponse.builder()
                .title(title)
                .pages(pages)
                .build();
    }

    // 가독성을 위한 페이지 생성 헬퍼 메소드
    private PagedStoryResponse.Page createPage(int num, String text, String bg, String mood, String action) {
        return PagedStoryResponse.Page.builder()
                .pageNumber(num)
                .text(text.trim())
                .background(bg.trim())
                .mood(mood.trim())
                .action(action.trim())
                // imagePrompt는 세 항목을 합쳐서 생성하거나 별도 필드로 유지
                .imagePrompt(String.format("%s, %s, %s", bg, action, mood)) 
                .build();
    }    

    
    private PagedStoryResponse parseToPagedResponse222(String rawResponse, int expectedPageCount) {
        List<String> lines = Arrays.stream(rawResponse.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        String title = "";
        List<PagedStoryResponse.Page> pages = new ArrayList<>();

        String currentText = "";
        String currentPrompt = "";
        String currentBackground = "";
        String currentMood = "";
        String currentAction = "";
        int pageNum = 0;

        for (String line : lines) {
            if (line.startsWith("제목:")) {
                title = line.replace("제목:", "").trim();
                continue;
            }

            if (line.startsWith("#### 페이지 ")) {
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
                currentBackground = "";
                currentMood = "";
                currentAction = "";
                continue;
            }

            if (line.startsWith("imagePrompt:")) {
                currentPrompt = line.replace("imagePrompt:", "").trim();
            } else if (!line.isEmpty()) {
                currentText += line + "\n";
            }

            if (line.startsWith("**background:")) {
                currentBackground = line.replace("**background:", "").trim();
            } else if (!line.isEmpty()) {
                currentText += line + "\n";
            }
            
            if (line.startsWith("**mood:")) {
                currentMood = line.replace("**mood:", "").trim();
            } else if (!line.isEmpty()) {
                currentText += line + "\n";
            }

            if (line.startsWith("**action:")) {
            	currentAction = line.replace("**action:", "").trim();
            } else if (!line.isEmpty()) {
                currentText += line + "\n";
            }
//            log.info("currentBackground:: {}", currentBackground);            
//            log.info("currentMood:: {}", currentMood);            
log.info("currentAction:: {}", currentAction);            
            
        }

        log.info("pageNum:: {}", pageNum);            
        log.info("currentText:: {}", currentText);            
        
        // 마지막 페이지 저장
        if (pageNum > 0 && !currentText.isEmpty()) {
            pages.add(PagedStoryResponse.Page.builder()
                    .pageNumber(pageNum)
                    .text(currentText.trim())
                    .imagePrompt(currentPrompt.trim())
                    .background(currentBackground.trim())
                    .mood(currentMood.trim())
                    .action(currentAction.trim())
                    .build());
        }
        log.info("pageNum:: {}", pageNum);            
      log.info("pages:: {}", pages);            

        // 페이지 수가 맞지 않으면 로그 남기기
        if (pageNum != expectedPageCount) {
            log.warn("요청한 페이지 수({})와 실제 생성 페이지 수({})가 다릅니다", expectedPageCount, pages.size());
        }

        return PagedStoryResponse.builder()
                .title(title)
                .pages(pages)
                .build();
    }
    
	
}
