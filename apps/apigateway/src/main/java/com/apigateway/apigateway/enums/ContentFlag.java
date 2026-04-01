package com.apigateway.apigateway.enums;

public enum ContentFlag {
    SAFE("SAFE"),
    RISK("RISK"),
    UNASSIGNED("UNASSIGNED")
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
