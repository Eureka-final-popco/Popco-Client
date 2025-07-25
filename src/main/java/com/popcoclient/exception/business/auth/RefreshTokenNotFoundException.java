package com.popcoclient.exception.business.auth;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class RefreshTokenNotFoundException extends BusinessException {
    public RefreshTokenNotFoundException() {
        super(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    public RefreshTokenNotFoundException(String message) {
        super(ErrorCode.REFRESH_TOKEN_NOT_FOUND, message);
    }
}
