package com.popcoclient.user.dto.response;

import com.popcoclient.user.entity.User;
import com.popcoclient.user.entity.UserDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetailResponseDto {
    private Long userId;
    private String email;
    private String nickname;
    private String profileImageUrl;

    public static UserDetailResponseDto from(UserDetail userDetail) {
        return UserDetailResponseDto.builder()
                .userId(userDetail.getUserId())
                .email(userDetail.getUser().getEmail())
                .nickname(userDetail.getNickname())
                .profileImageUrl(userDetail.getProfilePath())
                .build();
    }
}
