package com.popcoclient.exception.business.review;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class AlreadyReviewedException extends BusinessException {
    public AlreadyReviewedException() {
        super(ErrorCode.ALREADY_REVIEWED);
    }

    public AlreadyReviewedException(String message) {
        super(ErrorCode.ALREADY_REVIEWED, message);
    }
}
