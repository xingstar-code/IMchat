package dev.xing.infinitechat.messagingservice.model.enums;

public enum SessionType {
    SINGLE(1),
    GROUP(2);
    private int value;

    SessionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
