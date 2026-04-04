package com.mrs.shakes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mrs.shakes.domain.story.StoryMaster;
import com.mrs.shakes.domain.story.StoryStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryMasterRepository extends JpaRepository<StoryMaster, Long> {

    // 1. 특정 상태의 스토리 목록 조회 (예: 생성 중인 것만 가져오기)
    List<StoryMaster> findAllByStatus(StoryStatus status);

    // 2. Fetch Join을 사용하여 페이지 정보까지 한 번에 가져오기 (성능 최적화)
    // 2차 교정 루프를 돌 때 N+1 문제를 방지하기 위해 필수입니다.
    @Query("SELECT s FROM StoryMaster s LEFT JOIN FETCH s.pages WHERE s.id = :id")
    Optional<StoryMaster> findByIdWithPages(@Param("id") Long id);

    // 3. 진행 중인 스토리 중 가장 최근 것 조회
    Optional<StoryMaster> findFirstByOrderByCreatedAtDesc();
}