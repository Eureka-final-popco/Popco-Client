package com.popcoclient.persona.entity;

import com.popcoclient.persona.entity.enums.PersonaType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "persona_details")
@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PersonaDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long personaDetailId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private PersonaType personaType;
    private String imgPath;

    @ManyToOne
    @JoinColumn(name = "persona_id")
    private Persona persona;
}
