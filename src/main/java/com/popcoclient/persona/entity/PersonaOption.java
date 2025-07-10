package com.popcoclient.persona.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PersonaOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long personaOptionId;

    @ManyToOne
    @JoinColumn(name = "persona_id")
    private Persona persona;

    @ManyToOne
    @JoinColumn(name = "option_id")
    private Option option;

    private Double score;
}
