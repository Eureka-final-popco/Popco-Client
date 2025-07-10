package com.popcoclient.exception.business.review;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class ReviewNotFoundException extends BusinessException {
    public ReviewNotFoundException() {
        super(ErrorCode.REVIEW_NOT_FOUND);
    }

    public ReviewNotFoundException(String message) {
        super(ErrorCode.REVIEW_NOT_FOUND, message);
    }
}
