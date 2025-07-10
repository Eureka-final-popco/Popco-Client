package com.popcoclient.persona.repository;

import com.popcoclient.persona.entity.PersonaOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaOptionRepository extends JpaRepository<PersonaOption, Long> {
}
