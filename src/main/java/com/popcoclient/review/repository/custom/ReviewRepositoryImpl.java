package com.popcoclient.review.repository.custom;

import com.popcoclient.content.entity.Content;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.popcoclient.review.dto.response.ReviewResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.popcoclient.review.entity.QReview.review;
import static com.popcoclient.review.entity.QReviewReaction.reviewReaction;
import static com.popcoclient.user.entity.QUser.user;
import static com.popcoclient.user.entity.QUserDetail.userDetail;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    // 로그인한 사용자면 작성자여부 판단, 비로그인은 False 반환
    private BooleanExpression reviewUserIdExist(Long userId) {
        if(userId == null)
            return Expressions.FALSE;
        return review.user.userId.eq(userId);
    }

    // 로그인한 사용자면 좋아요여부 판단, 비로그인은 False 반환
    private BooleanExpression reviewLikeUserIdExist(Long userId) {
        if(userId == null)
            return Expressions.FALSE;
        return JPAExpressions
                .selectOne()
                .from(reviewReaction)
                .where(reviewReaction.user.userId.eq(userId)
                        .and(reviewReaction.review.reviewId.eq(review.reviewId)))
                .exists();
    }

    // 리뷰 조회
    @Override
    public Page<ReviewResponseDto> findReviewList(Long userId, Content contentId, Pageable pageable) {
        List<ReviewResponseDto> reviewList = jpaQueryFactory
                .select(Projections.constructor(ReviewResponseDto.class,
                        review.reviewId,
                        review.user.userId,
                        userDetail.nickname,
                        userDetail.profilePath,
                        review.createdAt,
                        review.score,
                        review.text,
                        review.likeCount,
                        reviewLikeUserIdExist(userId),
                        reviewUserIdExist(userId)
                ))
                .from(review)
                .join(review.user, user)
                .leftJoin(userDetail).on(userDetail.user.eq(user))
                .where(review.content.eq(contentId))
                .orderBy(review.createdAt.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        // 개수 count
        JPAQuery<Long> count = jpaQueryFactory
                .select(review.count())
                .from(review)
                .where(review.content.eq(contentId));

        return PageableExecutionUtils.getPage(reviewList, pageable, count::fetchOne);
    }

    // 별점 평균
    @Override
    public Double avgStar(Content contentId) {
        return jpaQueryFactory
                .select(review.score.avg())
                .from(review)
                .where(review.content.eq(contentId))
                .fetchOne();
    }

}