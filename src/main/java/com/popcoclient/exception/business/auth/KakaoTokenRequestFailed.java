package com.popcoclient.exception.business.auth;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class KakaoTokenRequestFailed extends BusinessException {
    public KakaoTokenRequestFailed() {
        super(ErrorCode.KAKAO_TOKEN_REQUEST_FAILED);
    }

    public KakaoTokenRequestFailed(String message) {
        super(ErrorCode.KAKAO_TOKEN_REQUEST_FAILED, message);
    }
}
