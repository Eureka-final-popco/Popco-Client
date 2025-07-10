package com.popcoclient.collection.repository;

import com.popcoclient.collection.entity.CollectionReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionReactionRepository extends JpaRepository<CollectionReaction, Long> {
}
