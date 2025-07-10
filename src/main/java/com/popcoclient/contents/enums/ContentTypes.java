package com.popcoclient.contents.enums;

import lombok.Getter;

@Getter
public enum ContentTypes {
    MOVIE("영화"),
    SERIES("시리즈");

    private final String displayName;

    ContentTypes(String displayName) {
        this.displayName = displayName;
    }
}
