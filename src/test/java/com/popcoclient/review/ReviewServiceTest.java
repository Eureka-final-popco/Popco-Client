package com.popcoclient.review;

import com.popcoclient.content.entity.Content;
import com.popcoclient.content.entity.enums.ContentTypes;
import com.popcoclient.content.repository.ContentRepository;
import com.popcoclient.review.dto.response.ReviewLikeResponseDto;
import com.popcoclient.review.entity.Review;
import com.popcoclient.review.entity.enums.ReviewStatus;
import com.popcoclient.review.repository.ReviewReactionRepository;
import com.popcoclient.review.repository.ReviewRepository;
import com.popcoclient.review.service.ReviewService;
import com.popcoclient.user.entity.User;
import com.popcoclient.user.entity.UserDetail;
import com.popcoclient.user.repository.UserDetailRepository;
import com.popcoclient.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Slf4j
public class ReviewServiceTest {
    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewReactionRepository reviewReactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager; // JPA EntityManager

    private Review testReview;
    private List<User> testUsers;

    @BeforeEach
    void setUp() {
        // 테스트용 데이터 초기화
        cleanupTestData();
        createTestData();
    }

//    @Test
//    @DisplayName("동시성 테스트")
//    void multiThreadsLikesTest() throws InterruptedException {
//        int numberOfThreads = 500;
//        int numberOfUsers = 500;
//
//        // 초기 상태 검증
//        Review initialReview = reviewRepository.findById(testReview.getReviewId()).orElseThrow();
//        long initialLikeCount = initialReview.getLikeCount();
//        long initialReactionCount = reviewReactionRepository.countByReview(initialReview);
//
//        log.info("=== 초기 상태 ===");
//        log.info("초기 좋아요 카운트: {}", initialLikeCount);
//        log.info("초기 ReviewReaction 개수: {}", initialReactionCount);
//
//        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
//        CountDownLatch latch = new CountDownLatch(numberOfUsers);
//        List<Future<ReviewLikeResponseDto>> futures = new ArrayList<>();
//
//        // 여러 사용자가 동시에 좋아요를 누름
//        for (int i = 0; i < numberOfUsers; i++) {
//            Long userId = testUsers.get(i).getUserId();
//            Future<ReviewLikeResponseDto> future = executor.submit(() -> {
//                try {
//                    return reviewService.reactionReview(testReview.getReviewId(), userId);
//                } finally {
//                    latch.countDown();
//                }
//            });
//            futures.add(future);
//        }
//
//        // 모든 스레드가 완료될 때까지 대기
//        latch.await();
//        executor.shutdown();
//
//        // Future 결과 수집 (예외 처리 포함)
//        List<ReviewLikeResponseDto> results = new ArrayList<>();
//        List<Exception> exceptions = new ArrayList<>();
//
//        for (Future<ReviewLikeResponseDto> future : futures) {
//            try {
//                ReviewLikeResponseDto result = future.get();
//                results.add(result);
//            } catch (ExecutionException e) {
//                exceptions.add((Exception) e.getCause());
//                log.error("스레드 실행 중 예외 발생: {}", e.getCause().getMessage());
//            }
//        }
//
//        // 최종 상태 검증
//        Review finalReview = reviewRepository.findById(testReview.getReviewId()).orElseThrow();
//        long finalLikeCount = finalReview.getLikeCount();
//        long finalReactionCount = reviewReactionRepository.countByReview(finalReview);
//
//        log.info("=== 동시성 테스트 결과 ===");
//        log.info("성공한 요청 수: {}", results.size());
//        log.info("실패한 요청 수: {}", exceptions.size());
//        log.info("초기 좋아요 카운트: {}", initialLikeCount);
//        log.info("최종 좋아요 카운트: {}", finalLikeCount);
//        log.info("초기 ReviewReaction 개수: {}", initialReactionCount);
//        log.info("최종 ReviewReaction 개수: {}", finalReactionCount);
//        log.info("예상 증가량: {}", numberOfUsers);
//        log.info("실제 증가량: {}", finalLikeCount - initialLikeCount);
//
//        // 검증
//        assertThat(results.size()).isEqualTo(numberOfUsers);
//        assertThat(exceptions.size()).isEqualTo(0);
//        assertThat(finalLikeCount).isEqualTo(initialLikeCount + numberOfUsers);
//        assertThat(finalReactionCount).isEqualTo(initialReactionCount + numberOfUsers);
//
//        // 데이터 일관성 검증
//        assertThat(finalLikeCount).isEqualTo(finalReactionCount);
//
//        // 모든 결과가 좋아요 상태인지 확인
//        results.forEach(result -> {
//            assertThat(result.getIsLiked()).isTrue();
//        });
//
//        log.info("테스트 성공: 모든 검증 완료!");
//    }

    private void createTestData() {
        // 테스트용 Content 생성
        Content testContent = Content.builder()
                .contentId(1L)
                .title("Test Content")
                .type(ContentTypes.movie)
                .build();
        testContent = contentRepository.save(testContent);

        testUsers = new ArrayList<>();
        int numberOfUsersToCreate = 10;
        for (int i = 0; i < numberOfUsersToCreate; i++) {
            User user = User.builder()
                    .name("testUser_" + i)
                    .email("user" + i + "_" + System.nanoTime() + "@example.com")
                    .password("password" + i)
                    .build();
            userRepository.save(user);
            testUsers.add(user);
        }

        testReview = Review.builder()
                .reviewId(1L)
                .user(testUsers.get(0))
                .content(testContent)
                .text("This is a test review for concurrency")
                .score(BigDecimal.valueOf(5.0))
                .status(ReviewStatus.COMMON)
                .likeCount(0)
                .report(0)
                .build();
        reviewRepository.save(testReview);

        testReview = reviewRepository.findById(testReview.getReviewId())
                .orElseThrow(() -> new IllegalStateException("생성된 리뷰를 다시 찾을 수 없습니다."));

        log.info("생성된 테스트 리뷰 ID: {}", testReview.getReviewId()); // 다시 로드된 ID 확인
    }

    private void cleanupTestData() {
        // 기존 테스트 데이터 정리
        reviewReactionRepository.deleteAll();
        reviewReactionRepository.flush();
        reviewRepository.deleteAll();
        reviewRepository.flush();
        userDetailRepository.deleteAll();
        userDetailRepository.flush();
        userRepository.deleteAll();
        userRepository.flush();
    }

    @Test
    @DisplayName("토클 테스트")
    void multiThreadsToggleLikesTest() throws InterruptedException {
        // 토글 테스트: 같은 사용자가 여러 번 좋아요/좋아요 취소
        int numberOfThreads = 50;
        int numberOfTogglePerUser = 9;
        Long userId = testUsers.get(0).getUserId();

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfTogglePerUser);
        List<Future<ReviewLikeResponseDto>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfTogglePerUser; i++) {
            Future<ReviewLikeResponseDto> future = executor.submit(() -> {
                try {
                    return reviewService.reactionReview(testReview.getReviewId(), userId);
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }

        latch.await();
        executor.shutdown();

        // 결과 수집
        List<ReviewLikeResponseDto> results = new ArrayList<>();
        for (Future<ReviewLikeResponseDto> future : futures) {
            try {
                results.add(future.get());
            } catch (ExecutionException e) {
                log.error("토글 테스트 중 예외 발생: {}", e.getCause().getMessage());
            }
        }

        // 최종 상태 확인
        Review finalReview = reviewRepository.findById(testReview.getReviewId()).orElseThrow();
        long finalReactionCount = reviewReactionRepository.countByReview(finalReview);

        log.info("=== 토글 테스트 결과 ===");
        log.info("토글 시도 횟수: {}", numberOfTogglePerUser);
        log.info("최종 좋아요 카운트: {}", finalReview.getLikeCount());
        log.info("최종 ReviewReaction 개수: {}", finalReactionCount);

        // 홀수 번 토글하면 좋아요 상태, 짝수 번 토글하면 좋아요 취소 상태
        boolean expectedLikeState = (numberOfTogglePerUser % 2) == 1;
        long expectedCount = expectedLikeState ? 1 : 0;

        assertThat(finalReview.getLikeCount()).isEqualTo(expectedCount);
        assertThat(finalReactionCount).isEqualTo(expectedCount);

        log.info("토글 테스트 성공!");
    }
}
