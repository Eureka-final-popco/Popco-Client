package com.popcoclient.persona.enums;

import lombok.Getter;

@Getter
public enum Type {
    BABY("아기"),
    ADULT("어른");

    private final String displayName;

    Type(String displayName) {
        this.displayName = displayName;
    }
}
