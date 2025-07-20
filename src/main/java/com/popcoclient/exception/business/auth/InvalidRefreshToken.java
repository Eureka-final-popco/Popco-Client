package com.popcoclient.exception.business.auth;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class InvalidRefreshToken extends BusinessException {
    public InvalidRefreshToken() {
        super(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    public InvalidRefreshToken(String message) {
        super(ErrorCode.INVALID_REFRESH_TOKEN, message);
    }
}
