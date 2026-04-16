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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j

public class KakaoService {
    @Value("${kakao.client-id}")
    private String clientId;
    
    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    public String getAuthorizationUrl() {
        String state = UUID.randomUUID().toString().substring(0, 8);

        return "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&state=" + state 
                + "&response_type=code";
    }
    
    
    public Map<String, Object> getUserInfo(String code) {
        // 1. 인가 코드로 Access Token 받기
        String accessToken = getAccessToken(code);
        
        // 2. Access Token으로 사용자 정보 받기
        return getUserInfoByToken(accessToken);
    }

    public String getAccessToken(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        RestTemplate rt = new RestTemplate();

        // HTTP Header 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 설정
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);
        params.add("client_secret", clientSecret); // 설정했을 경우 필수

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

        // POST 요청
        ResponseEntity<Map> response = rt.exchange(tokenUrl, HttpMethod.POST, kakaoTokenRequest, Map.class);
        
        return (String) response.getBody().get("access_token");
    }

    public Map<String, Object> getUserInfoByToken(String accessToken) {
        String profileUrl = "https://kapi.kakao.com/v2/user/me";
        RestTemplate rt = new RestTemplate();

        // HTTP Header 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers);

        // POST/GET 요청 (둘 다 지원)
        ResponseEntity<Map> response = rt.exchange(profileUrl, HttpMethod.POST, kakaoProfileRequest, Map.class);
        
        return response.getBody();
    }    
    
}
