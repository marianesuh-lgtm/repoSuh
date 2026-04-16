package com.mrs.shakes.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NaverService {

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    /**
     * 네이버 아이디로 로그인 인증 요청 URL 생성
     */
    public String getAuthorizationUrl() {
        // state 토큰은 보안을 위해 랜덤 문자열 생성을 권장합니다.
        String state = UUID.randomUUID().toString().substring(0, 8);
        
        // 실제 운영 시에는 생성한 state를 세션에 저장한 뒤, 
        // 콜백 시점에 검증하는 로직을 추가하는 것이 좋습니다.
        
        return "https://nid.naver.com/oauth2.0/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&state=" + state;
    }

    public String getAccessToken(String code, String state) {
        RestTemplate restTemplate = new RestTemplate();
        
        String url = "https://nid.naver.com/oauth2.0/token"
                + "?grant_type=authorization_code"
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&code=" + code
                + "&state=" + state;

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        return (String) response.get("access_token");
    }

    public Map<String, Object> getUserProfile(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.GET,
                entity,
                Map.class
        );

        // 네이버 응답은 { response: { email: '...', id: '...' } } 구조입니다.
        Map<String, Object> body = response.getBody();
        return (Map<String, Object>) body.get("response");
    }

}