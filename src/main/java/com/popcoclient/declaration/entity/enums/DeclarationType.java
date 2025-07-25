package com.popcoclient.declaration.entity.enums;

import lombok.Getter;

@Getter
public enum DeclarationType {
    BADWORD("비속어/욕설이 포함된 리뷰"),
    SPOILER("스포일러가 포함된 리뷰"),
    IRRELEVANT("영화와 관련 없음"),
    ETC("기타");

    private final String description;

    DeclarationType(String description) {
        this.description = description;
    }

}

