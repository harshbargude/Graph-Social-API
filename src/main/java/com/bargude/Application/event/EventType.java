package com.bargude.Application.event;

public enum EventType {
    USER_CREATED("USER_CREATED"),
    USER_UPDATED("USER_UPDATED"),
    USER_FOLLOWED("USER_FOLLOWED"),
    USER_DELETED("USER_DELETED");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
