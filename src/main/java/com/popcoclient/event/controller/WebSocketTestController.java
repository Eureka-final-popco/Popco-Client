package com.popcoclient.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.popcoclient.event.dto.request.QuizSubmissionResultDto;
import com.popcoclient.event.service.impl.EventServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ws/test")
public class WebSocketTestController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper; // ObjectMapper
    private final EventServiceImpl quizService;

    @PostMapping("/broadcast")
    public void testBroadcast(@RequestParam String message) throws Exception{
        Map<String, Object> payloadMap = Map.of(
                "message", "테스트 메시지: " + message,
                "currentSurvivors", 99,
                "progressPercentage", 50.5
        );

        Map<String, Object> payloadMap2 = Map.of(
                "message", "테스트 메시지 2번 소켓: " + message,
                "currentSurvivors", 50,
                "progressPercentage", 10
        );

        String jsonPayload = objectMapper.writeValueAsString(payloadMap);
        String jsonPayload2 = objectMapper.writeValueAsString(payloadMap2);

        messagingTemplate.convertAndSend("/topic/quiz/123/question/1", jsonPayload);
        messagingTemplate.convertAndSend("/topic/quiz/123/question/2", jsonPayload2);
    }

    // 동시성 처리 테스트
    @PostMapping("/concurrent-submit")
    public Map<String, Object> testConcurrentSubmit(
            @RequestParam(defaultValue = "500") int userCount,
            @RequestParam Long quizId,
            @RequestParam Long questionId) {

        List<CompletableFuture<QuizSubmissionResultDto>> futures = new ArrayList<>();

        for (int i = 1; i <= userCount; i++) {
            final Long userId = (long) i;
            CompletableFuture<QuizSubmissionResultDto> future = CompletableFuture.supplyAsync(() ->
                    quizService.submitAnswer(quizId, questionId, userId, 1L)
            );
            futures.add(future);
        }

        List<QuizSubmissionResultDto> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        Map<String, Long> statusCount = results.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStatus().toString(),
                        Collectors.counting()
                ));

        return Map.of(
                "totalSubmits", userCount,
                "totalSubmissions", results.size(),
                "statusBreakdown", statusCount,
                "survivedCount", results.stream().filter(QuizSubmissionResultDto::isSurvived).count()
        );
    }
}
