package com.popcoclient.user.dto.request;

import lombok.Data;

@Data
public class UserRequestDto {
    private String email;
    private String name;
    private String password;
}
