package com.mrs.shakes.domain.character;

import java.util.List;

import com.mrs.shakes.dto.BookResponse;
import com.mrs.shakes.dto.BookResponse.PageItem;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor // JPA 엔티티는 기본 생성자가 필수입니다.
public class CharacterPrompt {
    @Id
    private String charId;
    private String appearance;
    private String personalityTraits;
    private String artStyle;
    private String negative;
}
