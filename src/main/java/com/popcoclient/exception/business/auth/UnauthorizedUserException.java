package com.popcoclient.exception.business.auth;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class UnauthorizedUserException extends BusinessException {
    public UnauthorizedUserException() {
        super(ErrorCode.UNAUTHORIZED_USER);
    }
    public UnauthorizedUserException(String message) {
        super(ErrorCode.UNAUTHORIZED_USER, message);
    }
}
