package com.mrs.shakes.config;

import java.util.Arrays;

import javax.crypto.SecretKey;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

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
	        // 1. CORS 설정 연결 (WebConfig 설정을 시큐리티 레벨에서도 적용)
	        .cors(cors -> cors.configurationSource(request -> {
	            CorsConfiguration config = new CorsConfiguration();
	            config.setAllowedOrigins(Arrays.asList(
	                "https://www.myshakes.cc", 
	                "https://api.myshakes.cc",
	                "http://localhost:5173"
	            ));
	            config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
	            config.setAllowedHeaders(Arrays.asList("*"));
	            config.setAllowCredentials(true);
	            config.setExposedHeaders(Arrays.asList("Authorization"));
	            return config;
	        }))

	        // 2. CSRF 비활성화 (JWT 사용 시 필수)
	        .csrf(csrf -> csrf.disable())

	        // 3. 세션 정책 설정 (STATELESS)
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

	        // 4. 권한 설정
	        .authorizeHttpRequests(auth -> auth
	            // 소셜 로그인 관련 경로는 반드시 permitAll()
	            .requestMatchers("/", "/login/**", "/api/auth/**", "/api/auth/callback/**", "/oauth2/**",
	                           "/images/**", "/css/**", "/js/**", "/favicon.ico").permitAll()
	            
	            .requestMatchers("/api/admin/**", "/stories-mng/**").hasRole("ADMIN")
	            .requestMatchers("/api/story/**", "/api/chat/**", "/home/**", "/api/children/**").hasAnyRole("USER", "ADMIN")
	            
	            .anyRequest().authenticated()
	        )

	        // 5. OAuth2Login 비활성화 (이미 수동으로 처리 중이시므로)
	        .oauth2Login(oauth2 -> oauth2.disable())
	        
	        // 6. 로그아웃 설정
	        .logout(logout -> logout
	            .logoutSuccessUrl("/")
	            .invalidateHttpSession(true)
	        )

	        // 7. JWT 필터 순서 지정
	        .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), 
	                        UsernamePasswordAuthenticationFilter.class);

	    return http.build();
	}	
}
