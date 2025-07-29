package com.popcoclient.persona.service.impl;

import com.popcoclient.content.entity.Genre;
import com.popcoclient.exception.business.QuestionNotFoundException;
import com.popcoclient.persona.dto.response.*;
import com.popcoclient.persona.entity.Option;
import com.popcoclient.persona.entity.Persona;
import com.popcoclient.persona.entity.PersonaQuestion;
import com.popcoclient.persona.entity.UserPersona;
import com.popcoclient.persona.repository.*;
import com.popcoclient.persona.service.PersonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonaServiceImpl implements PersonaService {
    private final PersonaRepository personaRepository;
    private final PersonaQuestionRepository personaQuestionRepository;
    private final PersonaDetailRepository personaDetailRepository;
    private final UserPersonaRepository userPersonaRepository;
    private final PersonaGenreRepository personaGenreRepository;

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

    @Transactional(readOnly = true)
    @Override
    public MyPersonaResponseDto getPersona(Long userId) {
        List<UserPersona> myPersonas = userPersonaRepository.findTop2ByUserPersonaId_UserIdOrderByScoreDesc(userId);
        Map<String, Double> calcMap = UtilServiceImpl.calculatePercentages(myPersonas.get(0).getScore(), myPersonas.get(1).getScore());
        Optional<Persona> mainOptPersona = personaRepository.findById(myPersonas.get(0).getUserPersonaId().getPersonaId());
        Optional<Persona> subOptPersona = personaRepository.findById(myPersonas.get(1).getUserPersonaId().getPersonaId());
        Persona mainPersona = mainOptPersona.isPresent() ? mainOptPersona.get() : null;
        Persona subPersona = subOptPersona.isPresent() ? subOptPersona.get() : null;

        List<String> mainPerGenres = personaGenreRepository.findGenreNamesByPersona(mainPersona);

        if (calcMap.get("main_percentage") - calcMap.get("sub_percentage") < 3){ // 아기팝코
             MyPersonaResponseDto myPersonaResponseDto = MyPersonaResponseDto.builder()
                    .myPersonaName(mainPersona.getName())
                    .myPersonaImgPath(mainPersona.getBabyImgPath())
                    .myPersonaTags(mainPersona.getTag())
                    .myPersonaGenres(mainPerGenres)
                    .myPersonaDescription(mainPersona.getDescription())
                    .mainPersonaName(mainPersona.getName())
                    .mainPersonaPercent(calcMap.get("main_percentage"))
                    .mainPersonaImgPath(mainPersona.getAdultImgPath())
                    .subPersonaName(subPersona.getName())
                    .subPersonaPercent(calcMap.get("sub_percentage"))
                    .subPersonaImgPath(subPersona.getAdultImgPath())
                    .build();

             return myPersonaResponseDto;

        } else{
              MyPersonaResponseDto myPersonaResponseDto = MyPersonaResponseDto.builder()
                    .myPersonaName(mainPersona.getName())
                    .myPersonaImgPath(mainPersona.getAdultImgPath())
                    .myPersonaTags(mainPersona.getTag())
                    .myPersonaGenres(mainPerGenres)
                    .myPersonaDescription(mainPersona.getDescription())
                    .mainPersonaName(mainPersona.getName())
                    .mainPersonaPercent(calcMap.get("main_percentage"))
                    .mainPersonaImgPath(mainPersona.getAdultImgPath())
                    .subPersonaName(subPersona.getName())
                    .subPersonaPercent(calcMap.get("sub_percentage"))
                    .subPersonaImgPath(subPersona.getAdultImgPath())
                    .build();

              return myPersonaResponseDto;
        }
    }


}
