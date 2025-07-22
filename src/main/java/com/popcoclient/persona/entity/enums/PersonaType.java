package com.popcoclient.persona.entity.enums;

import lombok.Getter;

@Getter
public enum PersonaType {
    BABY("아기"),
    ADULT("어른");

    private final String displayName;

    PersonaType(String displayName) {
        this.displayName = displayName;
    }
}
