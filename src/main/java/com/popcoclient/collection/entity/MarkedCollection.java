package com.popcoclient.collection.entity;

import com.popcoclient.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Table(name = "marked_collection")
@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MarkedCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long markedCollectionId;

    @ManyToOne
    @JoinColumn(name = "collection_id")
    private Collection collection;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @CreatedDate
    private LocalDateTime createdAt;
}
