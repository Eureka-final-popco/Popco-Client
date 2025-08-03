package com.popcoclient.persona.controller;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.persona.dto.response.*;
import com.popcoclient.persona.service.PersonaService;
import com.popcoclient.persona.service.impl.GptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "페르소나", description = "페르소나 관련 API")
@RestController
@RequestMapping("/personas")
@RequiredArgsConstructor
public class personaController {
    private final PersonaService personaService;
    private final JwtProvider jwtProvider;
    private final GptService gptService;

    @Operation(summary = "나의 페르소나 조회", description = "페르소나 페이지의 Section 1 + 2 에 사용될 데이터, 나의 메인&서브&최종 페르소나를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<ApiResponse<MyPersonaResponseDto>> getPersonas() {
        Long userId = jwtProvider.getRequiredUserId();
        return ResponseEntity.ok(ApiResponse.success(personaService.getPersona(userId)));
    }

    @Operation(summary = "페르소나 통계 조회", description = "페르소나 페이지의 Section 3 + 4 에 사용될 데이터, List[남, 여], 연령대[10,20,~~,60], 나의 평균 별점, 페르소나 평균 별점, List[내 이벤트 참여 수, 페스소나 평균 이벤트 참여 수], 지금까지 개최된 이벤트 수, List[1달간 내가 남긴 리뷰 수, 1달간 페평 남긴 리뷰 수], List[나의 좋아요 수 %, 싫어요 수 %]")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/analysis")
    public ResponseEntity<ApiResponse<PersonaAnalysisResponseDto>> getPersonaAnalysis() {
        Long userId = jwtProvider.getRequiredUserId();
        return ResponseEntity.ok(ApiResponse.success(personaService.getPersonaAnalysis(userId)));
    }

    @Operation(summary = "페르소나 목록 조회", description = "시스템에 등록된 모든 페르소나의 목록을 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<PersonaListResponseDto>> getPersonaList() {
        PersonaListResponseDto response = personaService.getPersonaList();
        return ResponseEntity.ok(ApiResponse.success("get persona list success", response));
    }

    @Operation(summary = "페르소나 질문 조회", description = "질문 번호를 통해 페르소나 결정 질문을 차례대로 조회합니다.")
    @GetMapping("/question/{questionNumber}")
    public ResponseEntity<ApiResponse<PersonaQuestionResponseDto>> getQuestionList(@PathVariable("questionNumber") Integer questionNumber) {
        PersonaQuestionResponseDto response = personaService.getPersonaQuestion(questionNumber);
        return ResponseEntity.ok(ApiResponse.success("get question success", response));
    }

    @Operation(summary = "페르소나 대사 출력", description = "사용자의 페르소나 순위에 따라 대사를 출력합니다.")
    @PostMapping("/texts")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<ApiResponse<PersonaTextResponseDto>>> chat() {
        Long userId = jwtProvider.getRequiredUserId();
        return gptService.getPersonaText(userId)
                .map(result -> ResponseEntity.ok(ApiResponse.success(result)));
    }
}
