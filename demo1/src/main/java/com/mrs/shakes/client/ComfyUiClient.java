package com.mrs.shakes.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class ComfyUiClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${comfyui.workflow-resource:florenceWF.json}")
    private String workflowResource;

    @Value("${comfyui.poll.max-seconds:120}")
    private int pollMaxSeconds;

    @Value("${comfyui.poll.interval-ms:1000}")
    private int pollIntervalMs;

    public ComfyUiClient(
            RestTemplate restTemplate,
            @Value("${comfyui.base-url:http://suh.local:8188}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * 기존 코드와 호환을 위해 유지: prompt를 positive로 사용
     */
    public String generateImage(String prompt) {
        List<String> urls = generateImages(prompt, "", null);
        return urls.isEmpty() ? "" : urls.get(0);
    }

    /**
     * ComfyUI 워크플로우 템플릿(`src/main/resources/florenceWF.json`)에
     * positive/negative 등을 주입해 이미지 생성 요청을 수행합니다.
     *
     * overrides 키 예시: seed, steps, cfg, width, height, batchSize, ckptName, samplerName, scheduler, denoise
     */
    public List<String> generateImages(String positivePrompt, String negativePrompt, Map<String, Object> overrides) {
        String template = loadWorkflowTemplate();
        String workflowJson = applyPlaceholders(template, positivePrompt, negativePrompt, overrides);

        JsonNode workflowNode;
        try {
            workflowNode = objectMapper.readTree(workflowJson);
        } catch (IOException e) {
            throw new RuntimeException("ComfyUI workflow JSON 파싱 실패: " + e.getMessage(), e);
        }

        Map<String, Object> body = Map.of(
                "prompt", workflowNode,
                "client_id", UUID.randomUUID().toString()
        );

        String url = baseUrl + "/prompt";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            log.info("request::: {}", request);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            if (response == null || response.get("prompt_id") == null) {
                throw new RuntimeException("ComfyUI /prompt 응답에 prompt_id가 없습니다: " + response);
            }
            String promptId = String.valueOf(response.get("prompt_id"));
            return pollForImages(promptId);
        } catch (RestClientException e) {
            log.error("ComfyUI request failed: {}", e.getMessage(), e);
            throw new RuntimeException("ComfyUI 요청 실패: " + e.getMessage(), e);
        }
    }

    private String loadWorkflowTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource(workflowResource);
            if (!resource.exists()) {
                throw new RuntimeException("workflow resource not found on classpath: " + workflowResource);
            }
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("workflow resource 로드 실패: " + e.getMessage(), e);
        }
    }

    private String applyPlaceholders(String template, String positive, String negative, Map<String, Object> overrides) {
        String seed = String.valueOf(overrides != null && overrides.get("seed") != null ? overrides.get("seed") : 123456);
        String steps = String.valueOf(overrides != null && overrides.get("steps") != null ? overrides.get("steps") : 25);
        String cfg = String.valueOf(overrides != null && overrides.get("cfg") != null ? overrides.get("cfg") : 7);
        String width = String.valueOf(overrides != null && overrides.get("width") != null ? overrides.get("width") : 768);
        String height = String.valueOf(overrides != null && overrides.get("height") != null ? overrides.get("height") : 768);
        String batchSize = String.valueOf(overrides != null && overrides.get("batchSize") != null ? overrides.get("batchSize") : 1);
        String ckptName = String.valueOf(overrides != null && overrides.get("ckptName") != null
                ? overrides.get("ckptName")
                : "v1-5-pruned-emaonly.safetensors");
        String samplerName = String.valueOf(overrides != null && overrides.get("samplerName") != null ? overrides.get("samplerName") : "euler");
        String scheduler = String.valueOf(overrides != null && overrides.get("scheduler") != null ? overrides.get("scheduler") : "normal");
        String denoise = String.valueOf(overrides != null && overrides.get("denoise") != null ? overrides.get("denoise") : 1.0);

        String safePositive = (positive != null ? positive : "").replace("\\", "\\\\").replace("\"", "\\\"");
        String safeNegative = (negative != null ? negative : "").replace("\\", "\\\\").replace("\"", "\\\"");
        String safeCkpt = (ckptName != null ? ckptName : "").replace("\\", "\\\\").replace("\"", "\\\"");
        String safeSampler = (samplerName != null ? samplerName : "").replace("\\", "\\\\").replace("\"", "\\\"");
        String safeScheduler = (scheduler != null ? scheduler : "").replace("\\", "\\\\").replace("\"", "\\\"");

        return template
                .replace("__POSITIVE_PROMPT__", safePositive)
                .replace("__NEGATIVE_PROMPT__", safeNegative)
                .replace("__SEED__", seed)
                .replace("__STEPS__", steps)
                .replace("__CFG__", cfg)
                .replace("__WIDTH__", width)
                .replace("__HEIGHT__", height)
                .replace("__BATCH_SIZE__", batchSize)
                .replace("__CKPT_NAME__", safeCkpt)
                .replace("__SAMPLER_NAME__", safeSampler)
                .replace("__SCHEDULER__", safeScheduler)
                .replace("__DENOISE__", denoise);
    }

    private List<String> pollForImages(String promptId) {
        long deadline = System.currentTimeMillis() + (pollMaxSeconds * 1000L);

        while (System.currentTimeMillis() < deadline) {
            try {
                String historyUrl = baseUrl + "/history/" + promptId;
                String historyJson = restTemplate.exchange(historyUrl, HttpMethod.GET, null, String.class).getBody();
                if (historyJson != null && !historyJson.isBlank()) {
                    List<String> urls = extractImageUrlsFromHistory(historyJson, promptId);
                    if (!urls.isEmpty()) {
                        return urls;
                    }
                }
            } catch (Exception e) {
                log.warn("ComfyUI polling error (promptId={}): {}", promptId, e.getMessage());
            }

            try {
                Thread.sleep(pollIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("ComfyUI polling interrupted");
            }
        }

        throw new RuntimeException("ComfyUI 이미지 생성 타임아웃 (promptId=" + promptId + ")");
    }

    private List<String> extractImageUrlsFromHistory(String historyJson, String promptId) throws IOException {
        JsonNode root = objectMapper.readTree(historyJson);
        JsonNode promptNode = root.get(promptId);
        if (promptNode == null && root.elements().hasNext()) {
            promptNode = root.elements().next();
        }
        if (promptNode == null) return List.of();

        JsonNode outputs = promptNode.get("outputs");
        if (outputs == null || !outputs.isObject()) return List.of();

        List<String> urls = new ArrayList<>();
        outputs.fields().forEachRemaining(entry -> {
            JsonNode node = entry.getValue();
            JsonNode images = node.get("images");
            if (images != null && images.isArray()) {
                for (JsonNode img : images) {
                    String filename = img.path("filename").asText(null);
                    String subfolder = img.path("subfolder").asText("");
                    String type = img.path("type").asText("output");
                    if (filename != null && !filename.isBlank()) {
                        urls.add(buildViewUrl(filename, subfolder, type));
                    }
                }
            }
        });

        return urls;
    }

    private String buildViewUrl(String filename, String subfolder, String type) {
        String encFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        String encSubfolder = URLEncoder.encode(subfolder != null ? subfolder : "", StandardCharsets.UTF_8);
        String encType = URLEncoder.encode(type != null ? type : "output", StandardCharsets.UTF_8);
        return baseUrl + "/view?filename=" + encFilename + "&subfolder=" + encSubfolder + "&type=" + encType;
    }
}