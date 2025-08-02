package com.popcoclient.user.service;

import com.popcoclient.user.entity.WishList;

import java.util.List;

public interface WishListService {
    WishList addWishList(Long userId, Long contentId, String contentType);
    List<WishList> getWishLists(Long userId);
    void deleteWishList(Long userId, Long contentId, String contentType);
}
