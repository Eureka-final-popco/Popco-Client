package com.popcoclient.content.repository;

import com.popcoclient.content.entity.Content;
import com.popcoclient.content.entity.key.ContentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepository extends JpaRepository<Content, ContentId> {
}
