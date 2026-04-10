package com.mrs.shakes.domain.story;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "story_content")
@Getter @Setter
@NoArgsConstructor
public class StoryContent {

    @Id
    private Long masterId; // StoryMaster의 ID와 공유

    @OneToOne
    @MapsId // StoryMaster의 ID를 이 엔티티의 PK로 사용
    @JoinColumn(name = "master_id")
    private StoryMaster story;

    // 선택 코드들
    private String charCode;
    private String plaCode;
    private String modCode;
    private String eveCode;
    private String comCode;
    private String proCode;
    private String actCode;
    private String solCode;
    private String endCode;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private Boolean isVerified = false;
    private Integer viewCount = 0;
    private String tags;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}