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
import org.springframework.web.multipart.MultipartFile;

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

        // 닉네임 업데이트 (null 또는 빈 문자열 아닌 경우에만)
        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            userDetail.setNickname(request.getNickname());
        }

        // 프로필 이미지가 새로 들어온 경우에만 S3 업로드 및 삭제 처리
        MultipartFile newProfileImage = request.getProfileImageUrl();
        if (newProfileImage != null && !newProfileImage.isEmpty()) {
            // 기존 프로필 이미지 삭제
            s3Service.deleteFile(userDetail.getProfilePath());

            // 새로운 프로필 이미지 업로드 및 경로 설정
            String imageUuid = s3Service.uploadFile(newProfileImage);
            String profilePath = "/profile/" + imageUuid;
            userDetail.setProfilePath(profilePath);
        }

        userDetailRepository.save(userDetail);
    }

}
