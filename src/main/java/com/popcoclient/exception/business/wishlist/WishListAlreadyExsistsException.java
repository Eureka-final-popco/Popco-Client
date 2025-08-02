package com.popcoclient.exception.business.wishlist;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class WishListAlreadyExsistsException extends BusinessException {
    public WishListAlreadyExsistsException() {
        super(ErrorCode.WISHLIST_ALREADY_EXISTS);
    }

    public WishListAlreadyExsistsException(String message) {
        super(ErrorCode.WISHLIST_ALREADY_EXISTS, message);
    }
}
