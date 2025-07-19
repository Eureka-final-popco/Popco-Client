package com.popcoclient.content.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "crew_members")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"crewRoles"})
public class CrewMember {
    @Id
    private Long id; // TMDB person ID

    @Column(nullable = false, length = 500)
    private String name;

    @Column(name = "profile_path", length = 500)
    private String profilePath;

    @Column(name = "known_for_department", length = 100)
    private String knownForDepartment;

    @Column(name = "gender")
    private Integer gender;

    @OneToMany(mappedBy = "crewMember", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Crew> crewRoles = new ArrayList<>();
}
