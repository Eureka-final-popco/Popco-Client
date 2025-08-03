package com.popcoclient.persona.entity;

import com.popcoclient.content.entity.Genre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Table(name = "persona_genres")
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

    @Column(name = "score")
    private BigDecimal score;
}
