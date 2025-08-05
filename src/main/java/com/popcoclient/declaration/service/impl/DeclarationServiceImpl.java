package com.popcoclient.declaration.service.impl;

import com.popcoclient.declaration.dto.request.DeclarationCreateRequestDto;
import com.popcoclient.declaration.dto.response.DeclarationTypeResponseDto;
import com.popcoclient.declaration.entity.Declaration;
import com.popcoclient.declaration.entity.enums.DeclarationType;
import com.popcoclient.declaration.repository.DeclarationRepository;
import com.popcoclient.declaration.service.DeclarationService;
import com.popcoclient.exception.business.DeclarationAlreadyExistsException;
import com.popcoclient.exception.business.UserNotFoundException;
import com.popcoclient.exception.business.review.ReviewNotFoundException;
import com.popcoclient.review.entity.Review;
import com.popcoclient.review.entity.enums.ReviewStatus;
import com.popcoclient.review.repository.ReviewRepository;
import com.popcoclient.user.entity.User;
import com.popcoclient.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeclarationServiceImpl implements DeclarationService {
    private final DeclarationRepository declarationRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public List<DeclarationTypeResponseDto> getDeclarationTypes() {
        return Arrays.stream(DeclarationType.values())
                .map(DeclarationTypeResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createReviewDeclaration(DeclarationCreateRequestDto dto, Long userId, Long reviewerId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        Review review = reviewRepository.findById(reviewerId)
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다. reviewId: " + reviewerId));

        boolean existsDeclaration = declarationRepository.existsByReviewAndUser(review, user);

        if (existsDeclaration) {
            throw new DeclarationAlreadyExistsException("이미 해당 리뷰를 신고하였습니다.");
        }

        Declaration declaration = Declaration.of(user, review, dto);
        declarationRepository.save(declaration);
        updateReviewStatusByDeclaration(review);
    }

    private void updateReviewStatusByDeclaration(Review review) {
        int newCount = review.getReport() + 1;

        if (review.getStatus() != ReviewStatus.BLIND && newCount >= 5) {
            review.updateStatusAndReport(ReviewStatus.BLIND, newCount);
        } else {
            review.updateReport(newCount);
        }

        reviewRepository.save(review);
    }

}
