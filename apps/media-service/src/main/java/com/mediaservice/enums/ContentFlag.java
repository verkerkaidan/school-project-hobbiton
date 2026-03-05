package com.mediaservice.enums;

public enum ContentFlag {
    UNASSIGNED("Unassigned"),
    SAFE("Safe"),
    RISK("Risk")
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
