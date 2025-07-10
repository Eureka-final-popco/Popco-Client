package com.popcoclient.content.entity.enums;

import lombok.Getter;

@Getter
public enum ReactionType {
    LIKE("좋아요"),
    DISLIKE("싫어요");

    private final String reaction;

    ReactionType(String reaction) {
        this.reaction = reaction;
    }
}
