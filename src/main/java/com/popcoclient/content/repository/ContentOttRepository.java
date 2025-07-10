package com.popcoclient.content.repository;

import com.popcoclient.content.entity.ContentOtt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentOttRepository extends JpaRepository<ContentOtt, Long> {
}
