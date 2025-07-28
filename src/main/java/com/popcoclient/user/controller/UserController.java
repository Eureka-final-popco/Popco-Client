package com.popcoclient.user.controller;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.user.dto.request.PasswordChangeRequest;
import com.popcoclient.user.dto.request.UserDetailCreateRequestDto;
import com.popcoclient.user.dto.request.UserDetailUpdateRequestDto;
import com.popcoclient.user.dto.request.UserSignupRequestDto;
import com.popcoclient.user.dto.response.UserDetailResponseDto;
import com.popcoclient.user.dto.response.UserResponseDto;
import com.popcoclient.user.service.UserDetailService;
import com.popcoclient.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    // 사용자 생성
    @Operation(summary = "회원가입", description = "회원가입(사용자 생성)")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@Valid @RequestBody UserSignupRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(userService.createUser(request)));
    }

    // 중복 이메일 확인
    @Operation(summary = "이메일 중복 확인", description = "이미 사용중인 이메일이면 true, 사용 가능하면 false")
    @GetMapping("/email")
    public ResponseEntity<ApiResponse<Boolean>> existUserByEmail(@RequestParam String email) {
        return ResponseEntity.ok(ApiResponse.success(userService.existUserByEmail(email)));
    }

    // ID로 사용자 조회
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
    public ResponseEntity<ApiResponse<Void>> createUserDetail(@Valid @RequestBody UserDetailCreateRequestDto request) {
        Long userId = jwtProvider.getRequiredUserId();
        userDetailService.createUserDetail(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Create UserDetail Success", null));
    }

    @Operation(summary = "userId로 사용자 상세 수정", description = "userId로 사용자 상세 수정")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/details")
    public ResponseEntity<ApiResponse<Void>> updateUserDetail(@Valid @RequestBody UserDetailUpdateRequestDto request) {
        Long userId = jwtProvider.getRequiredUserId();
        userDetailService.updateUserDetail(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Update UserDetail Success", null));
    }

//    // 이메일로 사용자 조회
//    @Operation(summary = "이메일로 사용자 조회", description = "이메일로 사용자 조회")
//    @GetMapping("/email/{email}")
//    public ResponseEntity<ApiResponse<UserResponseDto>> getUserByEmail(@PathVariable String email) {
//        return ResponseEntity.ok(ApiResponse.success(userService.getUserByEmail(email)));
//    }
//
//    // 모든 사용자 조회
//    @Operation(summary = "모든 사용자 조회", description = "모든 사용자 조회")
//    @GetMapping
//    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers() {
//        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
//    }
//
//    // 이름으로 사용자 검색
//    @Operation(summary = "이름으로 사용자 검색", description = "이름으로 사용자 검색")
//    @GetMapping("/search")
//    public ResponseEntity<ApiResponse<List<UserResponseDto>>> searchUsersByName(@RequestParam String name) {
//        return ResponseEntity.ok(ApiResponse.success(userService.searchUsersByName(name)));
//    }
//
//    // 사용자 수정
//    @Operation(summary = "사용자 수정", description = "사용자 수정")
//    @PutMapping("/{id}")
//    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(@PathVariable Long id,
//                                                   @Valid @RequestBody UserSignupRequestDto request) {
//        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(id, request)));
//    }
//
//    // 사용자 삭제
//    @Operation(summary = "사용자 삭제", description = "사용자 삭제")
//    @DeleteMapping("/{id}")
//    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
//        userService.deleteUser(id);
//        return ResponseEntity.ok(ApiResponse.success("사용자가 성공적으로 삭제되었습니다.", null));
//    }
//
//    // 비밀번호 변경
//    @Operation(summary = "비밀번호 변경", description = "비밀번호 변경")
//    @PatchMapping("/{id}/password")
//    public ResponseEntity<ApiResponse<Void>> changePassword(@PathVariable Long id,
//                                                              @Valid @RequestBody PasswordChangeRequest request) {
//        userService.changePassword(id, request);
//        return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 변경되었습니다.", null));
//    }
}
