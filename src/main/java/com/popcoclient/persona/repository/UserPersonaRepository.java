package com.popcoclient.persona.repository;

import com.popcoclient.persona.entity.UserPersona;
import com.popcoclient.persona.entity.key.UserPersonaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPersonaRepository extends JpaRepository<UserPersona, UserPersonaId> {
    @Query("SELECT up FROM UserPersona up WHERE up.score = " +
            "(SELECT MAX(up2.score) FROM UserPersona up2 WHERE up2.userPersonaId.userId = up.userPersonaId.userId)")
    List<UserPersona> findAllUsersMainPersonas();
    List<UserPersona> findTop2ByUserPersonaId_UserIdOrderByScoreDesc(Long userId);
}
