package com.mediaservice.enums;

public enum ContentFlag {
    NONE("None"),
    LIGHT("Light"),
    MEDIUM("Medium"),
    HIGH("High"),
    ;
    private final String value;

    ContentFlag(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
