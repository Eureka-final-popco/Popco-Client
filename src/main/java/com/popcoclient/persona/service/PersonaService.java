package com.popcoclient.persona.service;

import com.popcoclient.persona.dto.response.MyPersonaResponseDto;
import com.popcoclient.persona.dto.response.PersonaListResponseDto;
import com.popcoclient.persona.dto.response.PersonaQuestionResponseDto;

public interface PersonaService {
    PersonaListResponseDto getPersonaList();
    PersonaQuestionResponseDto getPersonaQuestion(Integer questionNumber);
    MyPersonaResponseDto getPersona(Long userId);
}

