package com.popcoclient.persona.repository;

import com.popcoclient.persona.entity.PersonaGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaGenreRepository extends JpaRepository<PersonaGenre, Long> {
}
