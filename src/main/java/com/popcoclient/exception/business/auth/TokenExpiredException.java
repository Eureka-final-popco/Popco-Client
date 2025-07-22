package com.popcoclient.exception.business.auth;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class TokenExpiredException extends BusinessException {

    public TokenExpiredException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
