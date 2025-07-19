package com.popcoclient.content.repository;

import com.popcoclient.content.entity.CrewMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectorRepository extends JpaRepository<CrewMember, Long> {
}
