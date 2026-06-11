package dev.xing.infinitechat.messagingservice.model.enums;

public enum AccountStatusEnum {
    NORMAL(1),
    DISABLED(2),
    CANCELLED(3);

    private Integer code;
    AccountStatusEnum(Integer code){
        this.code = code;
    }
    public Integer getCode(){
        return this.code;
    }
}
