package com.popcoclient.content.entity;

import jakarta.persistence.*;
import lombok.*;
import org.apache.logging.log4j.util.Cast;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "actors")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"castRoles"})
public class Actor {
    @Id
    private Long id; // TMDB person ID

    @Column(nullable = false, length = 500)
    private String name;

    @Column(name = "profile_path", length = 500)
    private String profilePath;

    @Column(name = "gender")
    private Integer gender; // 0: Not specified, 1: Female, 2: Male, 3: Non-binary

    @OneToMany(mappedBy = "actor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CastMember> castRoles = new ArrayList<>();
}
