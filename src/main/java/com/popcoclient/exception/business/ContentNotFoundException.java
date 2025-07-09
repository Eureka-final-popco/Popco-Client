package com.popcoclient.exception.business;

import com.popcoclient.exception.BaseException;
import com.popcoclient.exception.ErrorCode;

public class ContentNotFoundException extends BaseException {
    public ContentNotFoundException() {
        super(ErrorCode.CONTENT_NOT_FOUND);
    }

    public ContentNotFoundException(String message) {
        super(ErrorCode.CONTENT_NOT_FOUND, message);
    }
}