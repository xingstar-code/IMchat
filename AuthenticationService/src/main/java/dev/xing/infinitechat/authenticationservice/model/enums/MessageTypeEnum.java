package dev.xing.infinitechat.authenticationservice.model.enums;

public enum MessageTypeEnum {
    TEXT_MESSAGE(1),
    PICTURE_MESSAGE(2),
    FILE_MESSAGE(3),
    VIDEO_MESSAGE(4);

    private Integer code;
    MessageTypeEnum(Integer code){
        this.code = code;
    }
    public Integer getCode(){
        return this.code;
    }


}
