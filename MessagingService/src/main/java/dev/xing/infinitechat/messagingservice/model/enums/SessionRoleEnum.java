package dev.xing.infinitechat.messagingservice.model.enums;

public enum SessionRoleEnum {
    GROUP_OWNER(1),
    ADMINISTRATOR(2),
    ORDINARY_USER(3);

    private Integer code;
    SessionRoleEnum(Integer code){
        this.code = code;
    }
    public Integer getCode(){
        return this.code;
    }
}
