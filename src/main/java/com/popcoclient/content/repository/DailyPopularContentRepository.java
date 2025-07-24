package com.popcoclient.content.repository;

import com.popcoclient.content.entity.DailyPopularContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyPopularContentRepository extends JpaRepository<DailyPopularContent, Long> {

}
