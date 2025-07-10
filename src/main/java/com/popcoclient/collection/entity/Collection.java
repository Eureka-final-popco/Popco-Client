package com.popcoclient.collection.entity;

import com.popcoclient.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Table(name = "collection")
@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long collectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    public User user;

    private String title;
    private String description;
    private Long saveCount;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
