package com.popcoclient.review.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_detail")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetail {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "nickname", length = 100)
    private String nickname;

    @Column(name = "profile_path")
    private String profilePath;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id")
    private Persona persona;

    private UserDetail(User user, Persona persona, String nickname, String profilePath, LocalDate birthdate) {
        this.user = user;
        this.persona = persona;
        this.nickname = nickname;
        this.profilePath = profilePath;
        this.birthdate = birthdate;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}