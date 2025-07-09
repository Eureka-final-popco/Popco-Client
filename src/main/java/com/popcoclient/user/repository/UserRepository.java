package com.popcoclient.user.repository;

import com.popcoclient.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);

    // 이름으로 사용자 찾기 (부분 일치)
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
    List<User> findByNameContaining(@Param("name") String name);

    // 이메일 존재 여부 확인
    boolean existsByEmail(String email);

    // 이름과 이메일로 사용자 찾기
    Optional<User> findByNameAndEmail(String name, String email);
}