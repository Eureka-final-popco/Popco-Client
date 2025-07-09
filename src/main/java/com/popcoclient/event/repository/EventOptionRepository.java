package com.popcoclient.event.repository;

import com.popcoclient.event.entity.EventOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventOptionRepository extends JpaRepository<EventOption, Long> {
    List<EventOption> findByQuestionQuestionId(Long questionId);
}
