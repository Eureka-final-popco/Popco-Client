package com.popcoclient.persona.repository;

import com.popcoclient.persona.entity.UserPersona;
import com.popcoclient.persona.entity.key.UserPersonaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPersonaRepository extends JpaRepository<UserPersona, UserPersonaId> {
    List<UserPersona> findTop2ByUserPersonaId_UserIdOrderByScoreDesc(Long userId);
}
