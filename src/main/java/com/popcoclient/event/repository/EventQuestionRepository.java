package com.popcoclient.event.repository;

import com.popcoclient.event.entity.EventQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventQuestionRepository extends JpaRepository<EventQuestion, Long> {
    List<EventQuestion> findByEventEventIdOrderBySortOrder(Long eventId);
}
