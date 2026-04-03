package com.mrs.shakes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. CSRF 설정: API 서버로 사용하거나 로컬 개발 시에는 비활성화
            .csrf(csrf -> csrf.disable())
            
            // 2. HTTP 요청 권한 설정
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers("/", "/login/**", "/oauth2/**", "/images/**").permitAll() // 인증 없이 접근 가능
//                .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
//            )
//임시로 열어둠
         // 2. 모든 요청에 대해 인증 없이 접근 허용 (임시 길 뚫기)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() 
            )            
            
            // 3. OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login") // 커스텀 로그인 페이지가 있다면 설정
                .defaultSuccessUrl("/main", true) // 로그인 성공 시 이동할 페이지
                // .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)) // 사용자 정보 처리 서비스
            )

            // 4. 로그아웃 설정
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
            );

        return http.build();
    }
	
}
