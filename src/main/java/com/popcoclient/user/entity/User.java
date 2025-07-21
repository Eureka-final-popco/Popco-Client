package com.popcoclient.user.entity;

import com.popcoclient.user.dto.request.UserSignupRequestDto;
import com.popcoclient.user.dto.response.UserResponseDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.context.properties.bind.DefaultValue;
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

    public static User of(UserSignupRequestDto dto, String encodedPassword) {
        return User.builder()
                .email(dto.getEmail())
                .name(dto.getName())
                .password(encodedPassword)
                .build();
    }
}
