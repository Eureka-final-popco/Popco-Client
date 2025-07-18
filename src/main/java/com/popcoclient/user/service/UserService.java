package com.popcoclient.user.service;

import com.popcoclient.auth.util.JwtToken;
import com.popcoclient.user.dto.request.PasswordChangeRequest;
import com.popcoclient.user.dto.request.UserLoginRequestDto;
import com.popcoclient.user.dto.request.UserRegisterRequestDto;
import com.popcoclient.user.dto.response.UserResponseDto;

import java.util.List;

public interface UserService {
    UserResponseDto createUser(UserRegisterRequestDto requestDto);
    JwtToken login(UserLoginRequestDto requestDto);

    List<UserResponseDto> getAllUsers();
    UserResponseDto getUserById(Long id);
    UserResponseDto getUserByEmail(String email);
    List<UserResponseDto> searchUsersByName(String name);

    UserResponseDto updateUser(Long id, UserRegisterRequestDto requestDto);
    void deleteUser(Long id);
    void changePassword(Long id, PasswordChangeRequest request);
}
