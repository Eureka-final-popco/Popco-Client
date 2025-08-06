package com.popcoclient.event.controller;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.event.dto.request.QuizSubmissionResultDto;
import com.popcoclient.event.dto.request.SubmitAnswerRequestDto;
import com.popcoclient.event.dto.response.CurrentQuestionResponseDto;
import com.popcoclient.event.dto.response.QuizQuestionResponseDto;
import com.popcoclient.event.dto.response.QuizStatusResponseDto;
import com.popcoclient.event.dto.response.SurvivorRankingResponse;
import com.popcoclient.event.service.impl.EventServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 🎯 퀴즈 답안 제출 REST API 컨트롤러
 * 
 * 실시간 퀴즈 이벤트에서 사용자의 답안 제출을 처리합니다.
 * 동시성 처리와 선착순 검증이 핵심 기능입니다.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/quizzes")
@Slf4j
@Tag(name = "실시간 이벤트 진행", description = "이벤트 진행중에 사용할 api 리스트")
public class QuizAnswerController {

    private final EventServiceImpl quizAnswerService;
    private final JwtProvider jwtProvider;

    @GetMapping("/latest")
    @Operation(summary = "가장 최근에 등록된 이벤트 퀴즈의 ID 조회", description = "이벤트 배너에 담기는 ID, 가장 최근의 이벤트 ID 값을 받아오는 API, 받아와서 해당 ID 페이지 이동, response.data 값 바로 이용")
    public ResponseEntity<ApiResponse<Long>> getQuizId() {
        return ResponseEntity.ok(ApiResponse.success(quizAnswerService.getQuizId()));
    }

    @GetMapping("/{quizId}/questions/{questionId}")
    @Operation(summary = "퀴즈 질문 및 선택지 리스트 조회", description = "이번 이벤트에서 사용될 n 번 질문과 선택지 리스트를 조회합니다.")
    public ResponseEntity<ApiResponse<CurrentQuestionResponseDto>> getQuizQuestion(@PathVariable Long quizId, @PathVariable Long questionId) {
        return ResponseEntity.ok(ApiResponse.success(quizAnswerService.getQuizQuestion(quizId, questionId)));
    }

    @PostMapping("/{quizId}/start")
    @Operation(summary = "퀴즈 시작 (문제1 타이머 시작), 테스트용", description = "이벤트 시작 시간이 되면 해당 메서드 호출, 이후 2번 문제부터는 자동화, 본 서버에서는 스케줄링으로 알아서 호출 예정, 프론트에서 요청하지 않아도 됨")
    public ResponseEntity<ApiResponse<String>> startQuiz(@PathVariable Long quizId) {
        quizAnswerService.startFirstQuestion(quizId);
        return ResponseEntity.ok(ApiResponse.success("퀴즈가 시작되었습니다"));
    }

    @PostMapping("/{quizId}/waiting")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "이벤트 페이지 접근 시 브로드캐스트", description = "사용자가 이벤트 페이지에 접근하면서 실행, 브로드캐스트의 트리거")
    public ResponseEntity<ApiResponse<Void>> waitQuiz(@PathVariable Long quizId) {
        Long userId = jwtProvider.getRequiredUserId();
        quizAnswerService.startEventWaitingBroadcast(quizId);
        return ResponseEntity.ok(ApiResponse.success("대기 타이머 브로드캐스트 시작!", null));
    }
    /**
     * 🎯 답안 제출 API
     */
    @PostMapping("/{quizId}/questions/{questionId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "답안 제출", description = "")
    public ResponseEntity<ApiResponse<QuizSubmissionResultDto>> submitAnswer(
            @PathVariable Long quizId,
            @PathVariable Long questionId,
            @Valid @RequestBody SubmitAnswerRequestDto request) {

        Long userId = jwtProvider.getRequiredUserId();
        return ResponseEntity.ok(ApiResponse.success(quizAnswerService.submitAnswer(quizId, questionId, userId, request.getOptionId())));
    }

    /**
     * 📊 퀴즈 현재 상태 조회 API
     */
    @Operation(summary = "퀴즈 현재 상태 조회", description = "현재 퀴즈의 진행 정보 (현재 생존자 수, 남은 시간) 을 조회하는 api")
    @GetMapping("/{quizId}/questions/{questionId}/status")
    public ResponseEntity<ApiResponse<QuizStatusResponseDto>> getQuizStatus(
            @PathVariable Long quizId,
            @PathVariable Long questionId) {

        return ResponseEntity.ok(ApiResponse.success(quizAnswerService.getQuizStatus(quizId, questionId)));
    }

    /**
     * 🏆 현재 생존자 순위 조회 API
     */
    @Operation(summary = "선착순 순위 조회", description = "사용자가 어떤 quiz 의 몇 번째 question 을 몇 등으로 통과했는지 조회하는 api")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{quizId}/questions/{questionId}/survivors")
    public ResponseEntity<ApiResponse<SurvivorRankingResponse>> getSurvivors(
            @PathVariable Long quizId,
            @PathVariable Long questionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(quizAnswerService.getSurvivorRanking(quizId, questionId, page, size)));
    }
}