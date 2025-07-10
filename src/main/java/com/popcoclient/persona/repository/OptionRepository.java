package com.popcoclient.persona.repository;

import com.popcoclient.persona.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionRepository extends JpaRepository <Option, Integer> {
}
