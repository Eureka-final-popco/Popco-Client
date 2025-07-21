package com.popcoclient.exception.business.auth;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class KakaoTokenParsingFailed extends BusinessException {
    public KakaoTokenParsingFailed() {
        super(ErrorCode.KAKAO_TOKEN_PARSING_FAILED);
    }

    public KakaoTokenParsingFailed(String message) {
        super(ErrorCode.KAKAO_TOKEN_PARSING_FAILED, message);
    }
}
