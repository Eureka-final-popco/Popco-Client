package com.popcoclient.contents.repository;

import com.popcoclient.contents.entity.Ott;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OttRepository extends JpaRepository<Ott, Long> {
}
