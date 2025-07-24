package com.popcoclient.auth.dto.response;

import com.popcoclient.auth.dto.response.enums.KakaoResultType;
import com.popcoclient.common.response.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KakaoLoginResponseDto {
    private KakaoResultType type;
    private ApiResponse<?> data;

    public static KakaoLoginResponseDto loginSuccess(ApiResponse<LoginResponseDto> loginData) {
        return new KakaoLoginResponseDto(KakaoResultType.LOGIN_SUCCESS, loginData);
    }

    public static KakaoLoginResponseDto signupRequired(ApiResponse<KakaoPreSignupResponseDto> signupData) {
        return new KakaoLoginResponseDto(KakaoResultType.SIGNUP_REQUIRED, signupData);
    }

    public boolean isLoginSuccess() { return type == KakaoResultType.LOGIN_SUCCESS; }

    @SuppressWarnings("unchecked")
    public <T> T getData() { return (T) data; }
}
