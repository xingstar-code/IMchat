package dev.xing.infinitechat.ContactService.enums;

public enum ConfigEnum {
    MEDIA_TYPE("application/json; charset=utf-8"),
    WORKED_ID("1"),
    DATACENTER_ID("1"),
    GROUP_AVATAR_URL("http://localhost:8080/img/avatar/IM_GROUP.jpg");


    private final String value;

    ConfigEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
