package com.popcoclient.contents.repository;

import com.popcoclient.contents.entity.ContentOtt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentOttRepository extends JpaRepository<ContentOtt, Long> {
}
