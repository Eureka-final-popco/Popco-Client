package com.popcoclient.user.service;

import com.popcoclient.user.dto.request.PasswordChangeRequest;
import com.popcoclient.user.dto.request.UserSignupRequestDto;
import com.popcoclient.user.dto.response.UserResponseDto;

import java.util.List;

public interface UserService {
    UserResponseDto createUser(UserSignupRequestDto requestDto);
    boolean existUserByEmail(String email);

    List<UserResponseDto> getAllUsers();
    UserResponseDto getUserById(Long id);
    UserResponseDto getUserByEmail(String email);
    List<UserResponseDto> searchUsersByName(String name);

    UserResponseDto updateUser(Long id, UserSignupRequestDto requestDto);
    void deleteUser(Long id);
    void changePassword(Long id, PasswordChangeRequest request);
}
