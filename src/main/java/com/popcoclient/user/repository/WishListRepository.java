package com.popcoclient.user.repository;

import com.popcoclient.user.entity.WishList;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishListRepository extends CrudRepository<WishList, Long> {
}
