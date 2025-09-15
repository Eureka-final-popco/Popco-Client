package com.popcoclient.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Table(name = "users")
@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String name;
    private String email;
    private String password;
    private boolean isActive;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime unbanAt;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private UserDetail userDetail;

    // 닉네임 getter 메소드 추가
    public String getNickname() {
        return userDetail != null ? userDetail.getNickname() : null;
    }
    public String getProfileUrl() {
        return userDetail != null ? userDetail.getProfilePath() : null;
    }

    public static User of(String email, String encodedPassword) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .build();
    }
}
