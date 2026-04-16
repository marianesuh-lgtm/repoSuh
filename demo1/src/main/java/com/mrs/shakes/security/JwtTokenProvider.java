package com.mrs.shakes.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.mrs.shakes.entity.User;

import java.security.Key; // 자바 기본 보안 패키지
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.springframework.security.core.Authentication;
//4. (참고) SecurityContext 관리
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@Component
//@RequiredArgsConstructor // 1. final이 붙은 필드들을 인자로 받는 생성자를 자동 생성
public class JwtTokenProvider {

    private final SecretKey key;
    private final UserDetailsService userDetailsService;

    // 토큰 유효 시간 (예: 24시간)
    private final long tokenValidityInMilliseconds = 1000L * 60 * 60 * 24;
   // private final long validityInMilliseconds ;
	private final long validityInMilliseconds;

    public JwtTokenProvider(
    		@Value("${jwt.secret:vmfhaltmghauslsdjswkaos7dkjsh23928302382039283029382}") String secret,
    	    UserDetailsService userDetailsService,
    	    @Value("${jwt.expiration-time:3600000}") long validityInMilliseconds            
    	    ) {  // 기본 1시간

    	this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.userDetailsService = userDetailsService;
        this.validityInMilliseconds = validityInMilliseconds;
        }

    /**
     * Naver 로그인 후 받은 email로 JWT 토큰 생성
     */
    public String createToken(User user) {
        //Date now = new Date();
    	//byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    	//SecretKey key = Keys.hmacShaKeyFor(keyBytes); 
    	String subject = user.getProvider() + ":" + user.getProviderId(); // "kakao:12345"
    	
    	if ("LOCAL".equals(user.getProvider())) {
    	    // 일반 로그인은 email을 식별자로 사용
    	    subject = "LOCAL:" + user.getEmail(); 
    	} else {
    	    // 소셜 로그인은 providerId를 식별자로 사용
    	    subject = user.getProvider() + ":" + user.getProviderId();
    	}
    	
    	Claims claims = Jwts.claims().setSubject(subject);
        claims.put("role", user.getRole()); // 관리자 여부 확인을 위해 넣음
        claims.put("nickname", user.getNickname());
        claims.put("profileImage", user.getProfileImage());

        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);
        
        return Jwts.builder()
        		.setClaims(claims)
                //.setSubject(subject)                    // email을 subject에 저장
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, key)                  // JJWT 0.12+ 방식
                .compact();
    }


    
 // 1. 토큰 유효성 검증
    public boolean validateToken(String token) {
    	//byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    	//SecretKey key = Keys.hmacShaKeyFor(keyBytes); 
       try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    
    // 2. 토큰에서 인증 정보 조회
    public Authentication getAuthentication(String token) {
    	try {
    		//byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    	//SecretKey key = Keys.hmacShaKeyFor(keyBytes); 
    	String identifier = getSubject(token);
    	log.info("Token Identifier(Subject): {}", identifier); // 1. 식별자 확인
    	//String email = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody().getSubject();
         
        // DB에서 권한을 다시 조회하거나, 토큰에 담긴 권한을 그대로 꺼내서 사용
        // 여기서는 간단히 이메일로 UserDetails를 로드하는 방식 사용
    	// 2. CustomUserDetailsService를 통해 유저 정보를 로드함
        // 여기서 loadUserByUsername(identifier)가 호출됩니다.
        UserDetails userDetails = userDetailsService.loadUserByUsername(identifier);
        log.info("Loaded User Authorities: {}", userDetails.getAuthorities()); // 2. 권한 확인
        
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    	} catch (Exception e) {
            log.error("인증 객체 생성 실패: {}", e.getMessage());
            return null; // 여기서 null을 반환하면 필터에서 403이 발생합니다.
        }
      }
    
    public String getSubject(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    
}