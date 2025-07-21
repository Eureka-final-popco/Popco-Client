package com.popcoclient.content.repository;

import com.popcoclient.content.entity.ContentVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentVideoRepository extends JpaRepository<ContentVideo, String> {
}
