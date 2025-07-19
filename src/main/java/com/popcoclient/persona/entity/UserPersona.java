package com.popcoclient.persona.entity;

import com.popcoclient.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "user_personas")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPersona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userPersonaId;

    private BigDecimal score;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "persona_id")
    private Persona persona;
}
