package com.popcoclient.persona.repository;

import com.popcoclient.persona.entity.PersonaDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaDetailRepository extends JpaRepository<PersonaDetail, Long> {
}
