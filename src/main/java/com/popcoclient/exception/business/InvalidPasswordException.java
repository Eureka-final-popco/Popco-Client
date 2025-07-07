package com.popcoclient.exception.business;

import com.popcoclient.exception.BaseException;
import com.popcoclient.exception.ErrorCode;

public class InvalidPasswordException extends BaseException {
    public InvalidPasswordException() {
        super(ErrorCode.INVALID_PASSWORD);
    }

    public InvalidPasswordException(String message) {
        super(ErrorCode.INVALID_PASSWORD, message);
    }
}
