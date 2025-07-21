package com.popcoclient.auth.kakao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Component
@Slf4j
public class KakaoUtil {
    @Value("${spring.kakao.auth.rest-api-key}")
    private String key;
    @Value("${spring.kakao.auth.redirect}")
    private String redirect;

    public KakaoToken requestToken(String accessCode) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", key);
        params.add("redirect_url", redirect);
        params.add("code", accessCode);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class);

        ObjectMapper objectMapper = new ObjectMapper();

        KakaoToken oAuthToken = null;

        try {
            oAuthToken = objectMapper.readValue(response.getBody(), KakaoToken.class);
            log.info("oAuthToken : " + oAuthToken.getAccess_token());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return oAuthToken;
    }

    public KakaoProfile requestProfile(KakaoToken kakaoToken) {
        RestTemplate restTemplate2 = new RestTemplate();
        HttpHeaders headers2 = new HttpHeaders();
        KakaoProfile kakaoProfile;

        headers2.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers2.add("Authorization", "Bearer " + kakaoToken.getAccess_token());

        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers2);

        ResponseEntity<String> response2 = restTemplate2.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                kakaoProfileRequest,
                String.class);

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            kakaoProfile = objectMapper.readValue(response2.getBody(), KakaoProfile.class);
        } catch (JsonProcessingException e) {
            log.info(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException();
        }

        return kakaoProfile;
    }
}
