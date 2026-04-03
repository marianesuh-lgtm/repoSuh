package com.mrs.shakes.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;

import com.mrs.shakes.dto.OAuthAttributes;
import com.mrs.shakes.entity.User;
import com.mrs.shakes.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
	
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
	    private final UserRepository userRepository;

	    @Override
	    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
	        OAuth2User oAuth2User = super.loadUser(userRequest);

	        String registrationId = userRequest.getClientRegistration().getRegistrationId();
	        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
	                .getUserInfoEndpoint().getUserNameAttributeName();

	        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

	        User user = saveOrUpdate(attributes);
	        
	        return new DefaultOAuth2User(
	        	    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole())),  
	        	    attributes.getAttributes(),
	        	    attributes.getNameAttributeKey());	     
	    }


	    

	    private User saveOrUpdate(OAuthAttributes attributes) {
	        // 1. providerId(소셜 고유 ID)로 기존 사용자가 있는지 조회합니다.
	        User user = userRepository.findByProviderId(attributes.getProviderId())
	                // 2. 이미 존재하는 유저라면 정보를 최신화합니다. (선택 사항)
	                .map(entity -> {
	                    entity.setNickname(attributes.getName());
	                    entity.setProfileImage(attributes.getPicture());
	                    return entity; 
	                })
	                // 3. 존재하지 않는 신규 유저라면 새 엔티티를 생성합니다.
	                .orElseGet(() -> attributes.toEntity());

	        // 4. DB에 저장 (신규는 INSERT, 기존은 UPDATE 쿼리가 실행됩니다)
	        return userRepository.save(user);
	    }	    
	    
	    
	}
