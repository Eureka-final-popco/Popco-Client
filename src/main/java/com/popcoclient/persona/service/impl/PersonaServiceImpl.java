package com.popcoclient.persona.service.impl;

import com.popcoclient.content.entity.enums.ReactionType;
import com.popcoclient.content.repository.ContentReactionRepository;
import com.popcoclient.event.repository.QuizRepository;
import com.popcoclient.event.repository.UserQuizAttemptRepository;
import com.popcoclient.exception.business.QuestionNotFoundException;
import com.popcoclient.persona.dto.response.*;
import com.popcoclient.persona.entity.Option;
import com.popcoclient.persona.entity.Persona;
import com.popcoclient.persona.entity.PersonaQuestion;
import com.popcoclient.persona.entity.UserPersona;
import com.popcoclient.persona.repository.PersonaGenreRepository;
import com.popcoclient.persona.repository.PersonaQuestionRepository;
import com.popcoclient.persona.repository.PersonaRepository;
import com.popcoclient.persona.repository.UserPersonaRepository;
import com.popcoclient.persona.service.PersonaService;
import com.popcoclient.review.repository.ReviewRepository;
import com.popcoclient.user.entity.UserDetail;
import com.popcoclient.user.repository.UserDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonaServiceImpl implements PersonaService {
    private final PersonaRepository personaRepository;
    private final PersonaQuestionRepository personaQuestionRepository;
    private final UserPersonaRepository userPersonaRepository;
    private final PersonaGenreRepository personaGenreRepository;
    private final UserDetailRepository userDetailRepository;
    private final ContentReactionRepository contentReactionRepository;
    private final ReviewRepository reviewRepository;
    private final UserQuizAttemptRepository userQuizAttemptRepository;
    private final QuizRepository quizRepository;

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

        if (calcMap.get("main_percentage") - calcMap.get("sub_percentage") < 8){ // 아기팝코
             MyPersonaResponseDto myPersonaResponseDto = MyPersonaResponseDto.builder()
                    .myPersonaName("아기" + mainPersona.getName())
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

    @Transactional(readOnly = true)
    @Override
    public PersonaAnalysisResponseDto getPersonaAnalysis(Long userId) {
        List<UserPersona> allUsersMainPersonaList = userPersonaRepository.findAllUsersMainPersonas();
        Map<Long, Long>  userPerMap = new HashMap<>();
        for (UserPersona userPersona : allUsersMainPersonaList) {
            userPerMap.put(userPersona.getUserPersonaId().getUserId(), userPersona.getUserPersonaId().getPersonaId());
        }
        Long myPerId = userPerMap.get(userId);
        List<Long> samePersonaUserList = new ArrayList<>();
        userPerMap.forEach((uId, pId) -> {
            if (pId.equals(myPerId)) {
                samePersonaUserList.add(uId);
            }
        });

        List<UserDetail> userDetailsList = userDetailRepository.findAllByUserIdIn(samePersonaUserList);
        List<Integer> genderDistribution = UtilServiceImpl.calculateGenderPercent(userDetailsList); // genderPercent
        List<Integer> ageDistribution = UtilServiceImpl.calculateBirthPercent(userDetailsList); // agePercent
        List<Double> ratingDistribution = getRatingDistribution(userId, samePersonaUserList); // ratingPercent
        List<Integer> eventDistribution = getEventDistribution(userId, samePersonaUserList); // eventPercent
        Long eventCount = quizRepository.count(); // eventCount;
        List<Integer> reviewDistribution = getReviewsDistribution(userId, samePersonaUserList); // reviewPercent
        List<Integer> myReactionDistribution = getReactionsDistribution(userId); // myLikePercent

        return PersonaAnalysisResponseDto.of(genderDistribution, ageDistribution, ratingDistribution, eventDistribution, eventCount, reviewDistribution, myReactionDistribution);
    }

    public List<Double> getRatingDistribution(Long userId, List<Long> samePersonaUserList) {
        List<Double> ratingDistribution = new ArrayList<>();
        Double myRatingAvg = reviewRepository.findAvgScoreByUserId(userId);
        Double perRatingAvg = reviewRepository.findAvgScoreForUsersInList(samePersonaUserList);

        ratingDistribution.add(Math.round(myRatingAvg * 100.0) / 100.0);
        ratingDistribution.add(Math.round(perRatingAvg * 100.0) / 100.0);

        return ratingDistribution;
    }

    public List<Integer> getEventDistribution(Long userId, List<Long> samePersonaUsersList) {
        Integer myParticipate = userQuizAttemptRepository.countByUser_UserId(userId);
        Integer perTotalParticipate = userQuizAttemptRepository.countByUser_UserIdIn(samePersonaUsersList);

        return UtilServiceImpl.calcPerAvg(myParticipate, perTotalParticipate, samePersonaUsersList.size());
    }

    public List<Integer> getReviewsDistribution(Long userId, List<Long> samePersonaUsersList) {
        YearMonth currentYearMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentYearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentYearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        Integer myReviews = reviewRepository.countByUser_UserIdAndCreatedAtBetween(userId, startOfMonth, endOfMonth);
        Integer personaTotalReviews = reviewRepository.countByUser_UserIdInAndCreatedAtBetween(samePersonaUsersList, startOfMonth, endOfMonth);

        return UtilServiceImpl.calcPerAvg(myReviews, personaTotalReviews, samePersonaUsersList.size());
    }

    public List<Integer> getReactionsDistribution(Long userId) {
        Integer myLikes = contentReactionRepository.countByUser_UserIdAndReaction(userId, ReactionType.LIKE);
        Integer myDisLikes = contentReactionRepository.countByUser_UserIdAndReaction(userId, ReactionType.DISLIKE);

        return UtilServiceImpl.calculateIntegerPercentages(myLikes, myDisLikes);
    }

}
