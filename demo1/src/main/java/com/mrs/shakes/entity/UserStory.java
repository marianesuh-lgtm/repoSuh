package com.mrs.shakes.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_stories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStory {

    @Id
    @Column(name = "story_id", length = 36, nullable = false)
    private String storyId;

    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "main_character", length = 50)
    private String mainCharacter;

    @Column(name = "selected_codes", columnDefinition = "TEXT")
    private String selectedCodes;

    @Column(name = "status", length = 20)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "is_favorite")
    private Boolean isFavorite;

    @Column(name = "feedback_comment", columnDefinition = "TEXT")
    private String feedbackComment;

    // 엔티티가 영속화되기 전에 UUID를 자동으로 생성하여 할당합니다.
    @PrePersist
    public void prePersist() {
        if (this.storyId == null) {
            this.storyId = UUID.randomUUID().toString();
        }
        if (this.isFavorite == null) {
            this.isFavorite = false;
        }
        if (this.rating == null) {
            this.rating = 0;
        }
    }
}