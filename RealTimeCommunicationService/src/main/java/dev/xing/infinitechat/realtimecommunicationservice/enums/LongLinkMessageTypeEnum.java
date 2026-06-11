package dev.xing.infinitechat.realtimecommunicationservice.enums;

public enum LongLinkMessageTypeEnum {
    MESSAGE(1),
    LOG_OUT(2);


    private Integer code;
    LongLinkMessageTypeEnum(Integer code){
        this.code = code;
    }
    public Integer getCode(){
        return this.code;
    }
}
