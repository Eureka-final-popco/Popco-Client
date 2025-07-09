package com.popcoclient.user.service;

import com.popcoclient.user.dto.request.PasswordChangeRequest;
import com.popcoclient.user.dto.request.UserRequestDto;
import com.popcoclient.user.dto.response.UserResponseDto;

import java.util.List;

public interface UserService {
    List<UserResponseDto> getAllUsers();
    UserResponseDto getUserById(Long id);
    UserResponseDto getUserByEmail(String email);
    List<UserResponseDto> searchUsersByName(String name);

    UserResponseDto createUser(UserRequestDto requestDto);
    UserResponseDto updateUser(Long id, UserRequestDto requestDto);
    void deleteUser(Long id);
    void changePassword(Long id, PasswordChangeRequest request);
}
