package com.example.demo.service;

import com.example.demo.client.ComfyUiClient;
import com.example.demo.dto.ImageGenerationRequest;
import com.example.demo.dto.ImageGenerationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageGenerationService {

    private final OllamaChatService ollamaChatService;
    private final ComfyUiClient comfyUiClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImageGenerationResponse generate(ImageGenerationRequest req) {
        String model = req.getOllamaModel();

        String detailedPromptQuestion = buildDetailedPromptQuestion(req);
        log.info("Ollama detailed prompt question: {}", detailedPromptQuestion);
        String detailedPrompt = ollamaChatService.ask(detailedPromptQuestion, model);

        String comfyPromptQuestion = buildComfyPromptQuestion(detailedPrompt, req);
        log.info("Ollama comfy prompt question: {}", comfyPromptQuestion);
        String comfyPromptRaw = ollamaChatService.ask(comfyPromptQuestion, model);

        ComfyPrompt comfyPrompt = parseComfyPrompt(comfyPromptRaw);

        // 사용자가 negative를 지정했다면 추가 결합
        String finalNegative = mergeNegative(comfyPrompt.negative(), req.getNegative());

        Map<String, Object> overrides = buildComfyOverrides(req);
        List<String> urls = comfyUiClient.generateImages(comfyPrompt.positive(), finalNegative, overrides);

        ImageGenerationResponse res = new ImageGenerationResponse();
        res.setSuccess(true);
        res.setMessage(null);
        res.setDetailedPrompt(detailedPrompt);
        res.setPositivePrompt(comfyPrompt.positive());
        res.setNegativePrompt(finalNegative);
        res.setImageUrls(urls);
        return res;
    }

    private String buildDetailedPromptQuestion(ImageGenerationRequest req) {
        // 프론트 입력을 조합해 “상세 묘사 프롬프트”를 만들도록 Ollama에 요청
        // 출력은 텍스트만(불필요한 설명/머리말 금지)로 강제
        String joined = joinNonBlank(
                "topic: " + req.getTopic(),
                "style: " + req.getStyle(),
                "background: " + req.getBackground(),
                "mood: " + req.getMood(),
                "lighting: " + req.getLighting(),
                "colorPalette: " + req.getColorPalette(),
                "composition: " + req.getComposition(),
                "extra: " + req.getExtra()
        );

        return """
                You are an expert prompt engineer for image generation.
                Create a single, highly detailed visual description prompt based on the user inputs.
                Requirements:
                - Write in English.
                - Be concrete (subject, materials, textures, environment, lighting, camera, composition).
                - Avoid copyrighted character names.
                - Return ONLY the prompt text. No markdown, no bullets, no explanation.

                User inputs:
                %s
                """.formatted(joined);
    }

    private String buildComfyPromptQuestion(String detailedPrompt, ImageGenerationRequest req) {
        String userNeg = req.getNegative();
        String negLine = (userNeg != null && !userNeg.isBlank())
                ? "User negative prompt to include: " + userNeg
                : "User negative prompt: (none)";

        return """
                You are an expert Stable Diffusion prompt writer for ComfyUI.
                Convert the following detailed visual description into SD prompts.
                Output STRICT JSON ONLY with this schema:
                {"positive":"...","negative":"..."}
                Rules:
                - positive: one line, comma-separated tags/phrases.
                - negative: one line, comma-separated.
                - Do NOT include markdown or any extra keys.
                - Do NOT wrap in triple backticks.

                %s

                Detailed description:
                %s
                """.formatted(negLine, detailedPrompt);
    }

    private ComfyPrompt parseComfyPrompt(String raw) {
        if (raw == null) {
            return new ComfyPrompt("", "");
        }
        String trimmed = raw.trim();
        try {
            JsonNode node = objectMapper.readTree(trimmed);
            String pos = node.path("positive").asText("");
            String neg = node.path("negative").asText("");
            return new ComfyPrompt(pos, neg);
        } catch (Exception ignored) {
            // Ollama가 JSON 외 텍스트를 섞어 반환하는 경우를 대비해 JSON 구간만 추출 시도
            int start = trimmed.indexOf('{');
            int end = trimmed.lastIndexOf('}');
            if (start >= 0 && end > start) {
                String json = trimmed.substring(start, end + 1);
                try {
                    JsonNode node = objectMapper.readTree(json);
                    String pos = node.path("positive").asText("");
                    String neg = node.path("negative").asText("");
                    return new ComfyPrompt(pos, neg);
                } catch (Exception e2) {
                    log.warn("Failed to parse comfy prompt JSON: {}", e2.getMessage());
                }
            }
        }

        // 최후 수단: raw 전체를 positive로 사용
        return new ComfyPrompt(trimmed, "");
    }

    private Map<String, Object> buildComfyOverrides(ImageGenerationRequest req) {
        Map<String, Object> map = new HashMap<>();
        if (req.getWidth() != null) map.put("width", req.getWidth());
        if (req.getHeight() != null) map.put("height", req.getHeight());
        if (req.getSteps() != null) map.put("steps", req.getSteps());
        if (req.getSeed() != null) map.put("seed", req.getSeed());
        if (req.getCfg() != null) map.put("cfg", req.getCfg());
        if (req.getBatchSize() != null) map.put("batchSize", req.getBatchSize());
        if (req.getCkptName() != null && !req.getCkptName().isBlank()) map.put("ckptName", req.getCkptName());
        if (req.getSamplerName() != null && !req.getSamplerName().isBlank()) map.put("samplerName", req.getSamplerName());
        if (req.getScheduler() != null && !req.getScheduler().isBlank()) map.put("scheduler", req.getScheduler());
        if (req.getDenoise() != null) map.put("denoise", req.getDenoise());
        return map;
    }

    private String mergeNegative(String base, String extra) {
        String b = base != null ? base.trim() : "";
        String e = extra != null ? extra.trim() : "";
        if (b.isBlank()) return e;
        if (e.isBlank()) return b;
        return b + ", " + e;
    }

    private String joinNonBlank(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p == null) continue;
            String t = p.trim();
            if (t.isBlank()) continue;
            if (!sb.isEmpty()) sb.append("\n");
            sb.append(t);
        }
        return sb.toString();
    }

    private record ComfyPrompt(String positive, String negative) {}
}

