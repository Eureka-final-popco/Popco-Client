package com.popcoclient.persona.repository;

import com.popcoclient.persona.entity.Persona;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonaRepository extends JpaRepository <Persona, Long> {

    @Override
    @EntityGraph(attributePaths = {"personaDetail"})
    List<Persona> findAll();

}
