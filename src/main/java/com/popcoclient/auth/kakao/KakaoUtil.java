package com.popcoclient.auth.kakao;

import org.springframework.beans.factory.annotation.Value;

public class KakaoUtil {
    @Value("${spring.kakao.auth.rest-api-key}")
    private String key;
    @Value("${spring.kakao.auth.redirect}")
    private String redirect;
}
