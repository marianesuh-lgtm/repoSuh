package com.mrs.shakes.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Param;
//import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mrs.shakes.domain.prompt.PromptTemplate;

@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {
    @Query("SELECT p.content FROM PromptTemplate p WHERE p.templateName = :templateName AND p.isActive = true")
    Optional<String> findActiveContentByName(@Param("name") String name);
}