package com.popcoclient.review.repository.custom;

import com.popcoclient.content.entity.Content;
import com.popcoclient.review.dto.response.MyReviewResponseDto;
import com.popcoclient.review.entity.QReview;
import com.popcoclient.user.entity.User;
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

import java.time.LocalDateTime;
import java.util.List;

import static com.popcoclient.review.entity.QReview.review;
import static com.popcoclient.review.entity.QReviewReaction.reviewReaction;
import static com.popcoclient.user.entity.QUser.user;
import static com.popcoclient.user.entity.QUserDetail.userDetail;
import static com.popcoclient.declaration.entity.QDeclaration.declaration;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    private BooleanExpression reviewUserIdExist(Long userId) {
        if(userId == null)
            return Expressions.FALSE;
        return review.user.userId.eq(userId);
    }

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

    private BooleanExpression reviewDeclarationUserIdExist(Long userId) {
        if(userId == null)
            return Expressions.FALSE;
        return JPAExpressions
                .selectOne()
                .from(declaration)
                .where(declaration.user.userId.eq(userId)
                        .and(declaration.review.reviewId.eq(QReview.review.reviewId)))
                .exists();
    }

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
                        reviewDeclarationUserIdExist(userId)
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

    @Override
    public List<MyReviewResponseDto> findReviewListByUserIdAndMonth(User user, LocalDateTime start, LocalDateTime end) {
        return jpaQueryFactory
                .select(Projections.constructor(MyReviewResponseDto.class,
                        review.reviewId,
                        review.content.contentId.id,
                        review.content.contentId.type,
                        review.content.title,
                        review.content.posterPath,
                        review.score,
                        review.text,
                        review.createdAt,
                        review.status.as("status"),
                        review.likeCount,
                        reviewLikeUserIdExist(user.getUserId())
                ))
                .from(review)
                .where(
                        review.user.eq(user),
                        review.createdAt.between(start, end)
                )
                .orderBy(review.likeCount.desc())
                .fetch();
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

}