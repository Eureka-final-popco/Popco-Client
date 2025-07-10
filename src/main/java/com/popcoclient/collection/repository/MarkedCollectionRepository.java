package com.popcoclient.collection.repository;

import com.popcoclient.collection.entity.MarkedCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarkedCollectionRepository extends JpaRepository<MarkedCollection, Long> {
}
