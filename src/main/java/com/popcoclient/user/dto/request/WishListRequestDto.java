package com.popcoclient.user.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WishListRequestDto {
    private Long contentId;
    private String contentType;
}
