package com.popcoclient.persona.entity.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.util.Objects;

@Embeddable
@Data
public class UserPersonaId {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "persona_id")
    private Long personaId;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserPersonaId userPersonaId)) return false;
        return Objects.equals(userId, userPersonaId.userId) &&
                Objects.equals(personaId, userPersonaId.personaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, personaId);
    }

    public static UserPersonaId of(Long userId, Long personaId) {
        UserPersonaId id = new UserPersonaId();
        id.userId = userId;
        id.personaId = personaId;
        return id;
    }
}
