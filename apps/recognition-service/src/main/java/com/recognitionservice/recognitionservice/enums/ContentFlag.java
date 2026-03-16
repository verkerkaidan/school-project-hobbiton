package com.recognitionservice.recognitionservice.enums;

public enum ContentFlag {
    UNASSIGNED("UNASSIGNED"),
    SAFE("SAFE"),
    RISK("RISK");

    private final String value;

    ContentFlag(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
