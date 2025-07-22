package com.popcoclient.user.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UserDetailUpdateRequestDto {
    private String nickname;
    private MultipartFile profileImageUrl;
}
