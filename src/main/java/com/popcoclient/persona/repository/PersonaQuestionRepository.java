package com.popcoclient.persona.repository;

import com.popcoclient.persona.entity.Persona;
import com.popcoclient.persona.entity.PersonaQuestion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaQuestionRepository extends JpaRepository<PersonaQuestion, Long> {

    @EntityGraph(attributePaths = {"options"})
    PersonaQuestion findBySortOrder(Integer sortOrder);

}
