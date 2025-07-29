package com.popcoclient.persona.entity;

import com.popcoclient.persona.entity.key.UserPersonaId;
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
    @EmbeddedId
    private UserPersonaId userPersonaId;

    private BigDecimal score;
}
