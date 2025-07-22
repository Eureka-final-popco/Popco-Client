package com.popcoclient.persona.service.impl;

import com.popcoclient.exception.business.QuestionNotFoundException;
import com.popcoclient.exception.business.UserNotFoundException;
import com.popcoclient.persona.dto.response.OptionResponseDto;
import com.popcoclient.persona.dto.response.PersonaListResponseDto;
import com.popcoclient.persona.dto.response.PersonaQuestionResponseDto;
import com.popcoclient.persona.dto.response.PersonaResponseDto;
import com.popcoclient.persona.entity.Option;
import com.popcoclient.persona.entity.Persona;
import com.popcoclient.persona.entity.PersonaQuestion;
import com.popcoclient.persona.repository.PersonaQuestionRepository;
import com.popcoclient.persona.repository.PersonaRepository;
import com.popcoclient.persona.service.PersonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonaServiceImpl implements PersonaService {
    private final PersonaRepository personaRepository;
    private final PersonaQuestionRepository personaQuestionRepository;

    @Override
    public PersonaListResponseDto getPersonaList() {
        List<Persona> personaList = personaRepository.findAll();

        List<PersonaResponseDto> personaResponseList = personaList.stream()
                .map(PersonaResponseDto::from)
                .collect(Collectors.toList());

        Long personaCount = personaRepository.count();

        return PersonaListResponseDto.of(personaResponseList, personaCount);
    }

    @Override
    public PersonaQuestionResponseDto getPersonaQuestion(Integer questionNumber) {
        PersonaQuestion question = personaQuestionRepository.findBySortOrder(questionNumber);

        if (question == null) {
            throw new QuestionNotFoundException("해당 질문 번호에 대한 질문이 존재하지 않습니다: " + questionNumber);
        }

        List<Option> options = question.getOptions();

        List<OptionResponseDto> optionResponseList = options.stream()
                .map(OptionResponseDto::from).toList();

        return PersonaQuestionResponseDto.from(question, optionResponseList);
    }


}
