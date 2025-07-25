package com.popcoclient.user.dto.request;

import lombok.Data;

@Data
public class UserSignupRequestDto {
    private String email;
    private String password;
}
