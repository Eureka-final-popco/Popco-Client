package com.popcoclient.content.repository;

import com.popcoclient.content.entity.WatchProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WatchProviderRepository extends JpaRepository<WatchProvider, Long> {
}
