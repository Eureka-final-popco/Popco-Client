package com.popcoclient.exception.business.review;

import com.popcoclient.exception.BaseException;
import com.popcoclient.exception.ErrorCode;

public class NotMyReviewException extends BaseException {
    public NotMyReviewException() {
        super(ErrorCode.NOT_MY_REVIEW);
    }

    public NotMyReviewException(String message) {
        super(ErrorCode.NOT_MY_REVIEW, message);
    }
}
