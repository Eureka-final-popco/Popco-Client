package com.popcoclient.persona.entity;

import com.popcoclient.persona.enums.Type;
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
public class PersonaDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long personaDetailId;

    @Enumerated(EnumType.STRING)
    private Type type;
    private String name;
    private String imgPath;

    @ManyToOne
    @JoinColumn(name = "persona_id")
    private Persona persona;
}
