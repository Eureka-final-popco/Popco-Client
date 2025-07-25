package com.popcoclient.declaration.entity;

import com.popcoclient.declaration.dto.request.DeclarationCreateRequestDto;
import com.popcoclient.declaration.entity.enums.DeclarationType;
import com.popcoclient.review.entity.Review;
import com.popcoclient.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "declarations")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Declaration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long declarationId;

    @ManyToOne
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "declaration_type")
    private DeclarationType declarationType;
    private String content;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static Declaration of(User user, Review review, DeclarationCreateRequestDto dto) {
        return Declaration.builder()
                .review(review)
                .user(user)
                .declarationType(DeclarationType.valueOf(dto.getDeclarationType()))
                .content(dto.getContent())
                .build();
    }
}
