package com.popcoclient.content.repository;

import com.popcoclient.content.entity.Genre;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {

    List<Genre> findByIdIn(Set<Integer> ids);
}
