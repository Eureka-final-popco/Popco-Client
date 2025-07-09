package com.popcoclient.exception.business.review;

import com.popcoclient.exception.BaseException;
import com.popcoclient.exception.ErrorCode;

public class AlreadyReviewedException extends BaseException {
    public AlreadyReviewedException() {
        super(ErrorCode.ALREADY_REVIEWED);
    }

    public AlreadyReviewedException(String message) {
        super(ErrorCode.ALREADY_REVIEWED, message);
    }
}
