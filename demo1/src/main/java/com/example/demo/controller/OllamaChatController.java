package com.example.demo.controller;

import com.example.demo.dto.ChatAnswerResponse;
import com.example.demo.dto.ChatQuestionRequest;
import com.example.demo.service.OllamaChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class OllamaChatController {

    private final OllamaChatService ollamaChatService;

    /**
     * 사용자 질문을 받아 Ollama(localhost:11434)에 전달하고 답변을 반환합니다.
     * POST /api/chat/ask
     * Body: { "question": "질문 내용", "model": "qwen-story" }  (model은 선택)
     */
    @PostMapping(value = "/ask", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ChatAnswerResponse ask(@RequestBody ChatQuestionRequest request) {
        String question = request.getQuestion();
        if (question == null || question.isBlank()) {
            return ChatAnswerResponse.builder()
                    .answer("질문(question)을 입력해 주세요.")
                    .model(request.getModel())
                    .build();
        }
        String model = request.getModel();
        String answer = ollamaChatService.ask(question, model);
        return ChatAnswerResponse.builder()
                .answer(answer)
                .model(model != null ? model : "default")
                .build();
    }
}
