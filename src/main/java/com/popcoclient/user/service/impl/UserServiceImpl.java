package com.popcoclient.user.service.impl;

import com.popcoclient.exception.business.EmailAlreadyExistsException;
import com.popcoclient.exception.business.UserNotFoundException;
import com.popcoclient.user.domain.User;
import com.popcoclient.user.dto.request.PasswordChangeRequest;
import com.popcoclient.user.dto.request.UserRequestDto;
import com.popcoclient.user.dto.response.UserResponseDto;
import com.popcoclient.user.repository.UserRepository;
import com.popcoclient.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + id));
        return convertToResponse(user);
    }

    @Override
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. Email: " + email));
        return convertToResponse(user);
    }

    public List<UserResponseDto> searchUsersByName(String name) {
        return userRepository.findByNameContaining(name).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponseDto createUser(UserRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new EmailAlreadyExistsException("이미 존재하는 이메일입니다: " + requestDto.getEmail());
        }

        User user = User.builder()
                .name(requestDto.getName())
                .email(requestDto.getEmail())
                .password(requestDto.getPassword())
//                .password(passwordEncoder.encode(requestDto.getPassword()))
                .build();

        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto requestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + id));

        // 이메일 변경 시 중복 확인
        if (!user.getEmail().equals(requestDto.getEmail()) &&
                userRepository.existsByEmail(requestDto.getEmail())) {
            throw new EmailAlreadyExistsException("이미 존재하는 이메일입니다: " + requestDto.getEmail());
        }

        user.setName(requestDto.getName());
        user.setEmail(requestDto.getEmail());

        User updatedUser = userRepository.save(user);
        return convertToResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void changePassword(Long id, PasswordChangeRequest request) {
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + id));
//
//        // 현재 비밀번호 확인
//        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
//            throw new InvalidPasswordException("현재 비밀번호가 올바르지 않습니다.");
//        }
//
//        // 새 비밀번호와 현재 비밀번호가 같은지 확인
//        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
//            throw new InvalidPasswordException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
//        }
//
//        // 비밀번호 확인 검증
//        if (!request.isPasswordMatching()) {
//            throw new InvalidPasswordException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
//        }
//
//        // 새 비밀번호 암호화 후 저장
//        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
//        userRepository.save(user);
    }

    private UserResponseDto convertToResponse(User user) {
        return new UserResponseDto(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
