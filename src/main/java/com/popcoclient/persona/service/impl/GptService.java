package com.popcoclient.persona.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.popcoclient.exception.business.UserNotFoundException;
import com.popcoclient.persona.dto.openai.GptRequest;
import com.popcoclient.persona.dto.openai.GptResponse;
import com.popcoclient.persona.dto.response.PersonaDetailDto;
import com.popcoclient.persona.dto.response.PersonaTextResponseDto;
import com.popcoclient.persona.repository.UserPersonaRepository;
import com.popcoclient.user.entity.User;
import com.popcoclient.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GptService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    @Value("${prompt.base-file-path}")
    private String basePromptFilePath;

    @Value("${prompt.base-file-path2}")
    private String basePromptFilePath2;

    private final UserPersonaRepository userPersonaRepository;
    private final UserRepository userRepository;

    private final ResourceLoader resourceLoader;
    private final WebClient webClient;
    private String basePromptTemplate;
    private String basePromptTemplate2;

    private final String userPrompt = "사용자의 페르소나 맞춤형 대사 생성해줘.";

    @Transactional(readOnly = true)
    public Mono<PersonaTextResponseDto> getPersonaText(Long userId) {
        Mono<String> personaText = generatePersonalizedResponse(userId);
        Mono<String> cleanPersonaText = cleanApiResponse(personaText);

        ObjectMapper mapper = new ObjectMapper();

        return cleanPersonaText.flatMap(jsonString -> {
            try {
                PersonaTextResponseDto dto = mapper.readValue(jsonString, PersonaTextResponseDto.class);
                return Mono.just(dto);
            } catch (JsonProcessingException e) {
                return Mono.error(e);
            }
        });
    }

    @PostConstruct
    public void loadBasePrompts() {
        try {
            Resource resource1 = resourceLoader.getResource(basePromptFilePath);
            basePromptTemplate = new String(
                    Files.readAllBytes(Paths.get(resource1.getURI())),
                    StandardCharsets.UTF_8
            );

            Resource resource2 = resourceLoader.getResource(basePromptFilePath2);
            basePromptTemplate2 = new String(
                    Files.readAllBytes(Paths.get(resource2.getURI())),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            basePromptTemplate = """
                당신은 도움이 되는 AI 어시스턴트입니다.
                
                사용자 정보:
                [1등 페르소나]
                - 페르소나 이름: {persona1}
                - 페르소나 설명 {description1}
                - 페르소나 태그 {tag1}
                - 페르소나 장르 {genres1}
                
                위 정보를 바탕으로 사용자에게 맞춤형 응답을 제공해주세요.
                사용자의 성향과 관심사를 고려하여 친근하고 도움이 되는 답변을 해주세요.
                """;
        }
    }

    private String buildSystemPrompt(List<PersonaDetailDto> personasList) {
        PersonaDetailDto p1 = personasList.get(0);
        PersonaDetailDto p2 = personasList.size() > 1 ? personasList.get(1) : null;
        BigDecimal diff = p1.getScore().subtract(p2 != null && p2.getScore() != null ? p2.getScore() : BigDecimal.ONE).abs();

        String selectedTemplate = diff.compareTo(BigDecimal.valueOf(10)) >= 0
                ? basePromptTemplate2
                : basePromptTemplate;

        String prompt = selectedTemplate
                .replace("{persona1}", p1.getName() != null ? p1.getName() : "사용자")
                .replace("{genres1}", p1.getPersonaGenre() != null ? p1.getPersonaGenre() : "정보 없음")
                .replace("{tag1}", p1.getTag() != null ? p1.getTag() : "정보 없음")
                .replace("{description1}", p1.getDescription() != null ? p1.getDescription() : "정보 없음");

        if (Objects.equals(selectedTemplate, basePromptTemplate)) {
            prompt = prompt
                    .replace("{persona2}", p2 != null && p2.getName() != null ? p2.getName() : "정보 없음")
                    .replace("{genres2}", p2 != null && p2.getPersonaGenre() != null ? p2.getPersonaGenre() : "정보 없음")
                    .replace("{tag2}", p2 != null && p2.getTag() != null ? p2.getTag() : "정보 없음")
                    .replace("{description2}", p2 != null && p2.getDescription() != null ? p2.getDescription() : "정보 없음")
                    .replace("{score}", diff.toString());
        }

        log.debug(prompt);
        return prompt;
    }

    private Mono<String> generatePersonalizedResponse(Long userId) {
        return Mono.fromCallable(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));

                    List<PersonaDetailDto> personaDetails = getPersonaDetails(user.getUserId(), PageRequest.of(0,2));

                    String systemPrompt = buildSystemPrompt(personaDetails);

                    List<GptRequest.Message> messages = new ArrayList<>();
                    messages.add(new GptRequest.Message("system", systemPrompt));
                    messages.add(new GptRequest.Message("user", userPrompt));

                    GptRequest request = new GptRequest(
                            model,
                            messages,
                            100,
                            0.7
                    );

                    log.debug("[generatePersonalizedResponse] 생성된 요청: {}", request);  // 요청 로그
                    return request;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSubscribe(sub -> log.info("[generatePersonalizedResponse] API 호출 시작"))
                .flatMap(gptRequest ->
                        webClient.post()
                                .uri(apiUrl)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                                .bodyValue(gptRequest)
                                .retrieve()
                                .bodyToMono(GptResponse.class)
                                .doOnNext(response -> log.debug("[generatePersonalizedResponse] API 응답: {}", response))
                                .map(response -> {
                                    if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                                        String content = response.getChoices().get(0).getMessage().getContent();
                                        log.debug("[generatePersonalizedResponse] 응답 내용: {}", content);
                                        return content;
                                    }
                                    return "응답을 생성할 수 없습니다.";
                                })
                )
                .doOnError(error -> log.error("[generatePersonalizedResponse] 오류 발생: {}", error.getMessage()))
                .onErrorReturn("오류가 발생했습니다. 사용자 정보를 확인하거나 다시 시도해주세요.");
    }

    private Mono<String> cleanApiResponse(Mono<String> rawResponse) {
        if (rawResponse == null) return Mono.empty();

        return rawResponse.map(str -> str
                .replace("```json\n", "")
                .replace("\n```", "")
                .replace("```", "")
                .replaceFirst("^:", "")
                .trim()
        );
    }

    private List<PersonaDetailDto> getPersonaDetails(Long userId, Pageable pageable) {
        return userPersonaRepository.findUserPersonasWithGenres(userId, pageable)
                .stream()
                .map(up -> PersonaDetailDto.builder()
                        .name(up.getPersona().getName())
                        .score(up.getScore())
                        .description(up.getPersona().getDescription())
                        .tag(up.getPersona().getTag())
                        .personaGenre(up.getPersona().getPersonaGenre().stream()
                                .map(pg -> pg.getGenre().getName())
                                .collect(Collectors.joining(", ")))  // List<String> → String
                        .build())
                .collect(Collectors.toList());
    }

}
