package com.popcoclient.exception.business.auth;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class RefreshTokenMisMatch extends BusinessException {
    public RefreshTokenMisMatch() {
        super(ErrorCode.REFRESH_TOKEN_MISMATCH);
    }

    public RefreshTokenMisMatch(String message) {
        super(ErrorCode.REFRESH_TOKEN_MISMATCH, message);
    }
}
