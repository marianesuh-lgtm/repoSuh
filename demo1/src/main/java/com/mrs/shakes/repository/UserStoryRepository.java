package com.mrs.shakes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mrs.shakes.entity.UserStory;

import java.util.List;

@Repository
public interface UserStoryRepository extends JpaRepository<UserStory, String> {
    
    // 특정 유저의 동화 리스트를 최신순으로 조회
    List<UserStory> findByUserIdOrderByCreatedAtDesc(String userId);
}