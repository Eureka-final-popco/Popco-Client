package com.popcoclient.exception.business.wishlist;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class WishListNotFoundException extends BusinessException {
    public WishListNotFoundException() {
        super(ErrorCode.WISHLIST_NOT_FOUND);
    }

    public WishListNotFoundException(String message) {
        super(ErrorCode.WISHLIST_NOT_FOUND, message);
    }
}
