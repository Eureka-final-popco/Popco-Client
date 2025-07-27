package com.popcoclient.review.repository.custom;

import com.popcoclient.content.entity.Content;
import com.popcoclient.content.entity.key.ContentId;
import com.popcoclient.review.entity.QReview;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.popcoclient.review.entity.QReview.review;
import static com.popcoclient.review.entity.QReviewReaction.reviewReaction;
import static com.popcoclient.user.entity.QUser.user;
import static com.popcoclient.user.entity.QUserDetail.userDetail;
import static com.popcoclient.declaration.entity.QDeclaration.declaration;

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

    // 로그인한 사용자면 좋아요 여부 판단, 비로그인은 False 반환
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

    // 로그인한 사용자면 리뷰 신고 여부 판단, 비로그인 False 반환
    private BooleanExpression reviewDeclarationUserIdExist(Long userId, QReview review) {
        if(userId == null)
            return Expressions.FALSE;
        return JPAExpressions
                .selectOne()
                .from(declaration)
                .where(declaration.user.userId.eq(userId)
                        .and(declaration.review.reviewId.eq(review.reviewId)))
                .exists();
    }

    // 리뷰 조회
    @Override
    public Page<ReviewResponseDto> findReviewList(Long userId, Content content, Pageable pageable, String sort) {
        OrderSpecifier<?> orderSpecifier;

        if ("popular".equalsIgnoreCase(sort)) {
            orderSpecifier = review.likeCount.desc();
        } else {
            orderSpecifier = review.createdAt.desc(); // 기본값: 최근순
        }

        List<ReviewResponseDto> reviewList = jpaQueryFactory
                .select(Projections.constructor(ReviewResponseDto.class,
                        review.reviewId,
                        review.user.userId,
                        userDetail.nickname,
                        userDetail.profilePath,
                        review.createdAt,
                        review.status,
                        review.score,
                        review.text,
                        review.likeCount,
                        reviewLikeUserIdExist(userId),
                        reviewUserIdExist(userId),
                        reviewDeclarationUserIdExist(userId, review)
                ))
                .from(review)
                .join(review.user, user)
                .leftJoin(userDetail).on(userDetail.user.eq(user))
                .where(review.content.eq(content))
                .orderBy(orderSpecifier)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        // 개수 count
        JPAQuery<Long> count = jpaQueryFactory
                .select(review.count())
                .from(review)
                .where(review.content.eq(content));

        return PageableExecutionUtils.getPage(reviewList, pageable, count::fetchOne);
    }

    // 별점 평균
    @Override
    public Double avgStar(Content content) {
        return jpaQueryFactory
                .select(review.score.avg())
                .from(review)
                .where(review.content.eq(content))
                .fetchOne();
    }

    @Override
    public Map<ContentId, Double> findAverageScoreByContents(Set<ContentId> contentIds) {
        if (contentIds.isEmpty()) return Collections.emptyMap();

        // 조건을 수동으로 조합
        BooleanBuilder builder = new BooleanBuilder();
        for (ContentId contentId : contentIds) {
            builder.or(
                    review.content.contentId.id.eq(contentId.getId())
                            .and(review.content.contentId.type.eq(contentId.getType()))
            );
        }

        List<Tuple> results = jpaQueryFactory
                .select(
                        review.content.contentId.id,
                        review.content.contentId.type,
                        review.score.avg()
                )
                .from(review)
                .where(builder)
                .groupBy(review.content.contentId.id, review.content.contentId.type)
                .fetch();

        // Map<ContentId, BigDecimal> 으로 변환
        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> new ContentId(tuple.get(review.content.contentId.id), tuple.get(review.content.contentId.type)),
                        tuple -> tuple.get(review.score.avg())
                ));
    }


}