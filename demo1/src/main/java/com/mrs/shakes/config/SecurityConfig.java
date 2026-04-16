package com.mrs.shakes.config;

import javax.crypto.SecretKey;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mrs.shakes.security.CustomUserDetailsService;
import com.mrs.shakes.security.JwtAuthenticationFilter;
import com.mrs.shakes.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // 1. final이 붙은 필드들을 인자로 받는 생성자를 자동 생성

public class SecurityConfig {
	
	private final JwtTokenProvider jwtTokenProvider;
	
	@Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	        .csrf(csrf -> csrf.disable())
	     // 세션을 사용하지 않으므로 STATELESS 설정 (JWT의 핵심)
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))	        
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/", "/login/**", "/api/auth/**", "/api/auth/callback/**",
	                           "/images/**", "/css/**", "/js/**", "/favicon.ico").permitAll()
	            
	            .requestMatchers("/api/admin/**", "/stories-mng/**").hasRole("ADMIN")
	            .requestMatchers("/api/story/**", "/api/chat/**","/home/**", "/fairytale/**").hasAnyRole("USER", "ADMIN")
	            
	            .anyRequest().authenticated()
	        )
	        
	        // OAuth2Login 설정은 지금 당신 상황에서는 거의 사용 안 하므로 최소화
	        .oauth2Login(oauth2 -> oauth2.disable())   // ← disable 추천 (커스텀으로 쓰고 있으니까)
	        
	        .logout(logout -> logout
	            .logoutSuccessUrl("/")
	            .invalidateHttpSession(true)
	        ) 

	 // [핵심] UsernamePasswordAuthenticationFilter 실행 전에 JWT 필터를 먼저 실행해라!
        .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), 
                        UsernamePasswordAuthenticationFilter.class);	    
	    return http.build();
	}	
}
