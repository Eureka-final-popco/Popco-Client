package com.popcoclient.user.repository;

import com.popcoclient.user.entity.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {
    List<UserDetail> findAllByUserIdIn(List<Long> userIds);
}
