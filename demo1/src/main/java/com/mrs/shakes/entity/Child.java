package com.mrs.shakes.entity;

import com.mrs.shakes.domain.user.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "children")
public class Child {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long childId;

    @Column(nullable = false)
    private String name;
    private int age;
    private String gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User parent; // 어떤 부모의 자녀인지 연결


    @Builder
    public Child(String name, int age, String gender) {
        this.name = name;
        this.age = age; // 추가
        this.gender = gender;
    }    
    


}
