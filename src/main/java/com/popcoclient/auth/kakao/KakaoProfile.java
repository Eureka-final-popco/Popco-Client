package com.popcoclient.auth.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class KakaoProfile {
    private Long id;
    private String connected_at;
    private KakaoAccount kakao_account;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    public static class KakaoAccount {
        private String email;
        private Profile profile;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Getter
        public static class Profile {
            private String nickname;
        }
    }
}
