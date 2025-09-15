package com.popcoclient.user.controller;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.user.dto.request.PasswordChangeRequest;
import com.popcoclient.user.dto.request.UserDetailCreateRequestDto;
import com.popcoclient.user.dto.request.UserDetailUpdateRequestDto;
import com.popcoclient.user.dto.request.UserSignupRequestDto;
import com.popcoclient.user.dto.response.UserCreateResponseDto;
import com.popcoclient.user.dto.response.UserDetailResponseDto;
import com.popcoclient.user.dto.response.UserResponseDto;
import com.popcoclient.user.service.UserDetailService;
import com.popcoclient.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "사용자 API", description = "유저 관련 CRUD")
public class UserController {
    private final UserService userService;
    private final UserDetailService userDetailService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "회원가입", description = "회원가입(사용자 생성)")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@Valid @RequestBody UserSignupRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(userService.createUser(request)));
    }

    @Operation(summary = "이메일 중복 확인", description = "이미 사용중인 이메일이면 true, 사용 가능하면 false")
    @GetMapping("/email")
    public ResponseEntity<ApiResponse<Boolean>> existUserByEmail(@RequestParam String email) {
        return ResponseEntity.ok(ApiResponse.success(userService.existUserByEmail(email)));
    }

    @Operation(summary = "userId로 사용자 상세 조회", description = "userId로 사용자 정보 조회")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/details")
    public ResponseEntity<ApiResponse<UserDetailResponseDto>> getUserDetail() {
        Long userId = jwtProvider.getRequiredUserId();
        UserDetailResponseDto response = userDetailService.getUserDetail(userId);
        return ResponseEntity.ok(ApiResponse.success("Get UserDetail Success",response));
    }

    @Operation(summary = "userId로 사용자 상세 입력", description = "userId로 사용자 상세 입력")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/details")
    public ResponseEntity<ApiResponse<UserCreateResponseDto>> createUserDetail(@Valid @RequestBody UserDetailCreateRequestDto request) {
        Long userId = jwtProvider.getRequiredUserId();
        UserCreateResponseDto response = userDetailService.createUserDetail(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Create UserDetail Success", response));
    }

    @Operation(summary = "userId로 사용자 상세 수정", description = "userId로 사용자 상세 수정")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping(value = "/details", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> updateUserDetail(@ModelAttribute UserDetailUpdateRequestDto request) {
        Long userId = jwtProvider.getRequiredUserId();
        userDetailService.updateUserDetail(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Update UserDetail Success", null));
    }
}
