package com.popcoclient.event.repository;

import com.popcoclient.event.entity.UserEventAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEventAttemptRepository extends JpaRepository<UserEventAttempt, Long> {
}
