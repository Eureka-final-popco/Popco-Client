package com.popcoclient.persona.controller;

import com.popcoclient.auth.jwt.JwtProvider;
import com.popcoclient.common.response.ApiResponse;
import com.popcoclient.persona.dto.response.PersonaListResponseDto;
import com.popcoclient.persona.dto.response.PersonaQuestionResponseDto;
import com.popcoclient.persona.dto.response.PersonaResponseDto;
import com.popcoclient.persona.service.PersonaService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("personas")
@RequiredArgsConstructor
public class personaController {
    private final PersonaService personaService;
    private final JwtProvider jwtProvider;

    @GetMapping
    public ResponseEntity<ApiResponse<PersonaListResponseDto>> getPersonaList() {
        PersonaListResponseDto response = personaService.getPersonaList();
        return ResponseEntity.ok(ApiResponse.success("get persona list success", response));
    }

    @GetMapping("/question/{questionNumber}")
    public ResponseEntity<ApiResponse<PersonaQuestionResponseDto>> getQuestionList(@PathVariable("questionNumber") Integer questionNumber) {
        PersonaQuestionResponseDto response = personaService.getPersonaQuestion(questionNumber);
        return ResponseEntity.ok(ApiResponse.success("get question success", response));
    }
}
