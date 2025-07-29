package com.popcoclient.persona.controller;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.persona.dto.response.MyPersonaResponseDto;
import com.popcoclient.persona.dto.response.PersonaAnalysisResponseDto;
import com.popcoclient.persona.dto.response.PersonaListResponseDto;
import com.popcoclient.persona.dto.response.PersonaQuestionResponseDto;
import com.popcoclient.persona.service.PersonaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "페르소나", description = "페르소나 관련 API")
@RestController
@RequestMapping("/personas")
@RequiredArgsConstructor
public class personaController {
    private final PersonaService personaService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "나의 페르소나 조회", description = "페르소나 페이지의 Section 1 + 2 에 사용될 데이터, 나의 메인&서브&최종 페르소나를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<ApiResponse<MyPersonaResponseDto>> getPersonas() {
        Long userId = jwtProvider.getUserIdFromAuthentication();
        return ResponseEntity.ok(ApiResponse.success(personaService.getPersona(userId)));
    }

    @Operation(summary = "페르소나 통계 조회", description = "페르소나 페이지의 Section 3 + 4 에 사용될 데이터, 개발중")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/analysis")
    public ResponseEntity<ApiResponse<PersonaAnalysisResponseDto>> getPersonaAnalysis() {
        return null;
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
}
