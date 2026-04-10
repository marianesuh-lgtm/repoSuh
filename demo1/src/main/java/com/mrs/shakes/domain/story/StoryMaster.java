package com.mrs.shakes.domain.story;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "story_master")
@Getter
@Setter
@NoArgsConstructor
public class StoryMaster {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String sideCharacterAppearance;
    
    // 캐릭터 설정을 별도 테이블 대신 JSONB로 관리하여 유연성 확보
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> characterSetting;

    private Integer totalPages;

    @Enumerated(EnumType.STRING)
    private StoryStatus status = StoryStatus.CREATED;

    private Integer processedPages = 0;

    private LocalDateTime startedAt = LocalDateTime.now();
    private LocalDateTime finishedAt;

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("pageNumber ASC")
    private List<StoryPage> pages = new ArrayList<>();
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @OneToOne(mappedBy = "story", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private StoryContent content;

    // 진행률 업데이트 로직
    public void incrementProcessedPages() {
        this.processedPages++;
        if (this.processedPages >= this.totalPages) {
            this.status = StoryStatus.COMPLETED;
            this.finishedAt = LocalDateTime.now();
        }
    }
    
 // 연관관계 편의 메서드
    public void setContent(StoryContent content) {
        this.content = content;
        content.setStory(this);
    }    
}


