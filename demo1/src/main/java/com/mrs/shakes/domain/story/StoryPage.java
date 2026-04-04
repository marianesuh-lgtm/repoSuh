package com.mrs.shakes.domain.story;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "story_pages")
public class StoryPage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    private StoryMaster story;

    private Integer pageNumber;
    private String phase;

    @Column(columnDefinition = "TEXT")
    private String rawContent;
    
    @Column(columnDefinition = "TEXT")
    private String refinedContent;

    // JSONB 매핑을 위해 @JdbcTypeCode(SqlTypes.JSON) 등을 사용 (Hibernate 6 기준)
    private List<String> keyVocabulary; 

    private String pageStatus; 
    
    private String rawImageKeywords;

    // ... getter/setter
}