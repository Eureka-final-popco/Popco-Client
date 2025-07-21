package com.popcoclient.user.entity;

import com.popcoclient.user.dto.request.UserDetailCreateRequestDto;
import com.popcoclient.user.dto.request.UserDetailUpdateRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_details")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserDetail {
    @Id
    private Long userId;
    private String nickname;
    private String profilePath;
    private LocalDate birthdate;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    public void updateFrom(UserDetailUpdateRequestDto request) {
        this.nickname = request.getNickname();
        this.profilePath = request.getProfileImageUrl();
    }

    public static UserDetail of(UserDetailCreateRequestDto request, User user) {
        return UserDetail.builder()
                .user(user)
                .nickname(request.getNickname())
                .birthdate(request.getBirthday())
                .build();
    }
}