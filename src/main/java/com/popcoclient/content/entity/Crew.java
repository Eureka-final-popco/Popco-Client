package com.popcoclient.content.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "crews")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Crew {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_member_id", nullable = false)
    private CrewMember crewMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "content_id", referencedColumnName = "id", nullable = false),
            @JoinColumn(name = "type", referencedColumnName = "type", nullable = false)
    })
    private Content content;

    private String job;
}

