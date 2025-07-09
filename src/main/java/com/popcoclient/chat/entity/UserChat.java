package com.popcoclient.chat.entity;

import com.popcoclient.review.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_chat")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long chatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false) // 메시지 내용이 길 수 있으므로 TEXT 타입으로 지정
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "sender", nullable = false)
    private String sender;
}
