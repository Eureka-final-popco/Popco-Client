package com.popcoclient.exception.business.review;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class NotMyReviewException extends BusinessException {
    public NotMyReviewException() {
        super(ErrorCode.NOT_MY_REVIEW);
    }

    public NotMyReviewException(String message) {
        super(ErrorCode.NOT_MY_REVIEW, message);
    }
}
