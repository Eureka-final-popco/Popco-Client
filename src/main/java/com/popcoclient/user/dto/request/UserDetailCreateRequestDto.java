package com.popcoclient.user.dto.request;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UserDetailCreateRequestDto {
    private String nickname;
    private LocalDate birthday;
}
