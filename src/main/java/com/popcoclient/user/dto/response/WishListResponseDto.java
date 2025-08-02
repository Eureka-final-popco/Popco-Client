package com.popcoclient.user.dto.response;

import com.popcoclient.user.entity.WishList;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishListResponseDto {
    private Long wishlistId;
    private Long userId;
    private Long contentId;
    private String contentType;
    private String contentTitle;
    private String contentPosterUrl;
    private LocalDateTime createdAt;

    public static WishListResponseDto from(WishList wishList) {
        if(wishList == null) {
            return null;
        }
        return WishListResponseDto.builder()
                .wishlistId(wishList.getWishlistId())
                .userId(wishList.getUser() != null ? wishList.getUser().getUserId() : null)
                .contentId(wishList.getContent() != null && wishList.getContent().getContentId() != null ?
                        wishList.getContent().getContentId().getId() : null)
                .contentType(wishList.getContent() != null && wishList.getContent().getContentId().getType() != null ?
                        wishList.getContent().getContentId().getType() : null)
                .contentTitle(wishList.getContent() != null ? wishList.getContent().getTitle() : null)
                .contentPosterUrl(wishList.getContent() != null ? wishList.getContent().getPosterPath() : null)
                .createdAt(wishList.getCreatedAt())
                .build();

    }
}
