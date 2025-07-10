package com.popcoclient.persona.entity;

import com.popcoclient.contents.entity.Genre;
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
public class PersonaGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long personaGenreId;

    @ManyToOne
    @JoinColumn(name = "persona_id")
    private Persona persona;

    @ManyToOne
    @JoinColumn(name = "genre_id")
    private Genre genre;
}
