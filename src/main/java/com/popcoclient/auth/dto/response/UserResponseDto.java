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
public class UserResponseDto {
    private String nickname;
    private String profileImgUrl;

    public static UserResponseDto from(UserDetail userDetail) {
        return UserResponseDto.builder()
                .nickname(userDetail.getNickname())
                .profileImgUrl(userDetail.getProfilePath())
                .build();
    }
}
