package com.popcoclient.user.repository;

import com.popcoclient.content.entity.Content;
import com.popcoclient.user.entity.User;
import com.popcoclient.user.entity.WishList;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishListRepository extends CrudRepository<WishList, Long> {
    List<WishList> findByUser(User user);
    Optional<WishList> findByUserAndContent(User user, Content content);
    void deleteByUserAndContent(User user, Content content);
}
