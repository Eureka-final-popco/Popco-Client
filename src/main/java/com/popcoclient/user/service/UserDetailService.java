package com.popcoclient.user.service;

import com.popcoclient.user.dto.request.UserDetailCreateRequestDto;
import com.popcoclient.user.dto.request.UserDetailUpdateRequestDto;
import com.popcoclient.user.dto.response.UserDetailResponseDto;

public interface UserDetailService {
    UserDetailResponseDto getUserDetail(Long userId);
    void createUserDetail(UserDetailCreateRequestDto request, Long userId);
    void updateUserDetail(UserDetailUpdateRequestDto request, Long userId);
}
