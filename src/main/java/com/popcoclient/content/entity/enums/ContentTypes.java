package com.popcoclient.content.entity.enums;

import lombok.Getter;

@Getter
public enum ContentTypes {
    movie("영화"),
    tv("시리즈");

    private final String displayName;

    ContentTypes(String displayName) {
        this.displayName = displayName;
    }
}
