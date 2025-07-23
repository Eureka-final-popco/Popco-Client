package com.popcoclient.auth.dto.response;

import com.popcoclient.user.entity.UserDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LoginUserResponseDto {
    private Long userId;
    private String nickname;
    private String profileImgUrl;

    public static LoginUserResponseDto from(UserDetail userDetail) {
        return LoginUserResponseDto.builder()
                .userId(userDetail.getUserId())
                .nickname(userDetail.getNickname())
                .profileImgUrl(userDetail.getProfilePath())
                .build();
    }
}
