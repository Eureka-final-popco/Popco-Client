package com.popcoclient.user.entity;

import com.popcoclient.user.dto.request.UserDetailCreateRequestDto;
import com.popcoclient.user.dto.request.UserDetailUpdateRequestDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_details")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserDetail {
    @Id
    private Long userId;
    private String nickname;
    private String profilePath;
    private String gender;
    private LocalDate birthdate;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    public static UserDetail of(UserDetailCreateRequestDto request, User user, String gender) {
        return UserDetail.builder()
                .user(user)
                .nickname(request.getNickname())
                .profilePath(null)
                .birthdate(request.getBirthday())
                .gender(gender)
                .build();
    }

    public static String parseGender(String gender) {
        if (gender == null) return null;

        return switch (gender.toUpperCase()) {
            case "MALE" -> "M";
            case "FEMALE" -> "F";
            default -> null; // 또는 gender 그대로 반환
        };
    }

}