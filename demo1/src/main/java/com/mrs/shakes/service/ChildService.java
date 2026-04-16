package com.mrs.shakes.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mrs.shakes.dto.ChildRequest;
import com.mrs.shakes.dto.ChildResponse;
import com.mrs.shakes.entity.Child;
import com.mrs.shakes.entity.User;
import com.mrs.shakes.repository.ChildRepository;
import com.mrs.shakes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChildService {
    private final ChildRepository childRepository;
    private final UserRepository memberRepository;

    @Transactional
    public void registerChild(ChildRequest dto, String identifier) {
        // 1. 토큰에서 추출한 이메일로 부모 조회
        User parent = memberRepository.findByEmailOrProviderId(identifier,identifier)
                .orElseThrow(() -> new RuntimeException("부모 정보를 찾을 수 없습니다."));

        log.info("identifier::{}",identifier);
        log.info("getName::{}", dto.getName());
        // 2. 자녀 정보 생성 및 저장
        Child child = Child.builder()
                .name(dto.getName())
                .age(dto.getAge())
                .gender(dto.getGender())
                .parent(parent) // 조회한 부모 객체 세팅
                .build();

        log.info("child::{}",child);
        
        childRepository.save(child);
    }
    
    @Transactional( readOnly = true)
    public List<ChildResponse> getMyChildren(String identifier) {
        // 1. 이메일로 부모 객체 찾기
        User parent = memberRepository.findByEmail(identifier)
        		.orElseGet(() -> memberRepository.findByEmailOrProviderId(identifier, identifier)
                        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.")));

        // 2. 해당 부모의 자녀들만 DB에서 조회
       // List<Child> children = childRepository.findByParent(parent);

        // 3. Entity 리스트를 DTO 리스트로 변환해서 반환 (순환 참조 방지)
        return childRepository.findByParent(parent).stream()
                .map(ChildResponse::from)
                .collect(Collectors.toList());
    }

}