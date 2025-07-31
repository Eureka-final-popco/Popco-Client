package com.popcoclient.user.service.impl;

import com.popcoclient.common.s3.service.S3Service;
import com.popcoclient.exception.business.UserDetailAlreadyExistsException;
import com.popcoclient.exception.business.UserNotFoundException;
import com.popcoclient.user.dto.request.UserDetailCreateRequestDto;
import com.popcoclient.user.dto.request.UserDetailUpdateRequestDto;
import com.popcoclient.user.dto.response.UserCreateResponseDto;
import com.popcoclient.user.dto.response.UserDetailResponseDto;
import com.popcoclient.user.entity.User;
import com.popcoclient.user.entity.UserDetail;
import com.popcoclient.user.repository.UserDetailRepository;
import com.popcoclient.user.repository.UserRepository;
import com.popcoclient.user.service.UserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailService {

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final S3Service s3Service;

    @Override
    public UserDetailResponseDto getUserDetail(Long userId) {
        UserDetail userDetail = userDetailRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자 정보를 찾을 수 없습니다. userId: " + userId));

        String profileImageUrl = s3Service.getFileUrl(userDetail.getProfilePath());
        return UserDetailResponseDto.of(userDetail, profileImageUrl);
    }

    @Override
    public UserCreateResponseDto createUserDetail(UserDetailCreateRequestDto request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        Optional<UserDetail> optionalUserDetail = userDetailRepository.findById(userId);

        if(optionalUserDetail.isPresent()) {
            throw new UserDetailAlreadyExistsException("이미 작성된 유저 정보가 존재합니다.");
        }

        UserDetail userDetail = UserDetail.of(request, user);
        userDetailRepository.save(userDetail);

        return new UserCreateResponseDto(user.getUserId());
    }

    @Override
    public void updateUserDetail(UserDetailUpdateRequestDto request, Long userId) {
        UserDetail userDetail = userDetailRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자 정보를 찾을 수 없습니다. userId: " + userId));

        s3Service.deleteFile(userDetail.getProfilePath());

        String imageUuid = s3Service.uploadFile(request.getProfileImageUrl());
        String profilePath = "/profile/" + imageUuid;
        userDetail.updateOf(request.getNickname(), profilePath);
        userDetailRepository.save(userDetail);
    }
}
