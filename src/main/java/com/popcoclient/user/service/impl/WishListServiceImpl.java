package com.popcoclient.user.service.impl;

import com.popcoclient.content.entity.Content;
import com.popcoclient.content.entity.key.ContentId;
import com.popcoclient.content.repository.ContentRepository;
import com.popcoclient.exception.business.ContentNotFoundException;
import com.popcoclient.exception.business.UserNotFoundException;
import com.popcoclient.exception.business.wishlist.WishListAlreadyExsistsException;
import com.popcoclient.exception.business.wishlist.WishListNotFoundException;
import com.popcoclient.user.entity.User;
import com.popcoclient.user.entity.WishList;
import com.popcoclient.user.repository.UserRepository;
import com.popcoclient.user.repository.WishListRepository;
import com.popcoclient.user.service.WishListService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishListServiceImpl implements WishListService {

    private final WishListRepository wishListRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;

    @Override
    @Transactional
    public WishList addWishList(Long userId, Long contentId, String contentType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자 정보를 찾을 수 없습니다. userId: " + userId));

        ContentId contentCompositeId = new ContentId(contentId, contentType);
        Content content = contentRepository.findById(contentCompositeId)
                .orElseThrow(() -> new ContentNotFoundException("콘텐츠 정보를 찾을 수 없습니다. contentId: " + contentId + ", contentType: " + contentType));

        if(wishListRepository.findByUserAndContent(user, content).isPresent()) {
            throw new WishListAlreadyExsistsException("이미 위시리스트에 존재하는 콘텐츠입니다. userId: " + userId + ", contentId: " + contentId + ", contentType: " + contentType);
        }

        WishList wishList = WishList.builder()
                .user(user)
                .content(content)
                .build();

        return wishListRepository.save(wishList);
    }

    @Override
    public List<WishList> getWishLists(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자 정보를 찾을 수 없습니다. userId: " + userId));
        return wishListRepository.findByUser(user);
    }

    @Override
    @Transactional
    public void deleteWishList(Long userId, Long contentId, String contentType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자 정보를 찾을 수 없습니다. userId: " + userId));

        ContentId contentCompositeId = new ContentId(contentId, contentType);
        Content content = contentRepository.findById(contentCompositeId)
                .orElseThrow(() -> new ContentNotFoundException("콘텐츠 정보를 찾을 수 없습니다. contentId: " + contentId + ", contentType: " + contentType));

        Optional<WishList> optionalWishList = wishListRepository.findByUserAndContent(user, content);
        if(optionalWishList.isEmpty()) {
            throw new WishListNotFoundException("위시리스트에서 해당 콘텐츠를 찾을 수 없습니다. userId: " + userId + ", contentId: " + contentId + ", contentType: " + contentType);
        }

        wishListRepository.deleteByUserAndContent(user, content);
    }
}
