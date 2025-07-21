package com.popcoclient.content.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cast_members")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CastMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "content_id", referencedColumnName = "id", nullable = false),
            @JoinColumn(name = "type", referencedColumnName = "type", nullable = false)
    })
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private Actor actor;

    @Column(name = "character_name", length = 100)
    private String characterName;

    @Column(nullable = false)
    private Integer castOrder;
}
