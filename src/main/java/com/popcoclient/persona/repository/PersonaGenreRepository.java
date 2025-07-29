package com.popcoclient.persona.repository;

import com.popcoclient.content.entity.Genre;
import com.popcoclient.persona.entity.Persona;
import com.popcoclient.persona.entity.PersonaGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonaGenreRepository extends JpaRepository<PersonaGenre, Long> {
    @Query("SELECT g.name FROM PersonaGenre pg JOIN pg.genre g WHERE pg.persona = :persona")
    List<String> findGenreNamesByPersona(@Param("persona") Persona persona);
}
