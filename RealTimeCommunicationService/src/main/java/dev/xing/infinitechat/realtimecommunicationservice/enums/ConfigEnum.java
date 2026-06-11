package dev.xing.infinitechat.realtimecommunicationservice.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ConfigEnum {

    SMS_ACCESS_KEY_ID("smsAccessKeyId", System.getenv().getOrDefault("ALIYUN_SMS_ACCESS_KEY_ID", "change-me")),
    SMS_ACCESS_KEY_SECRET("smsAccessKeySecret", System.getenv().getOrDefault("ALIYUN_SMS_ACCESS_KEY_SECRET", "change-me")),
    SMS_SIG_NAME("smsSigName","change-me"),
    SMS_TEMPLATE_CODE("smsTemplateCode", System.getenv().getOrDefault("ALIYUN_SMS_TEMPLATE_CODE", "change-me")),
    TOKEN_SECRET_KEY("tokenSecretKey", System.getenv().getOrDefault("TOKEN_SECRET_KEY", "change-me")),
    PASSWORD_SALT("passwordSalt", System.getenv().getOrDefault("PASSWORD_SALT", "change-me")),
    WX_STATE("wxState", System.getenv().getOrDefault("WX_STATE", "change-me")),
    WORKED_ID("workedId","1"),
    DATACENTER_ID("DATACENTER_ID","1"),
    IMAGE_URI("imageUri","http://localhost:8080/img/avatar/"),
    IMAGE_PATH("imagePath", "/home/img/avatar/"),
    NETTY_SERVER_HEAD("nettyServerHead","Nacos:"),
    REDIS_CONVERT_SEND("redisConvertSend","userLogout");

    private final String text;

    private final String value;

    ConfigEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }


    public static List<String> getValues() {
          return Arrays.stream(ConfigEnum.values()).map(ConfigEnum::getValue).collect(Collectors.toList());
    }


    public static ConfigEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (ConfigEnum anEnum : ConfigEnum.values()) {
            if (anEnum.getValue().equals(value)) {
                return anEnum;
            }

        }
        return null;
    }
    public String getText() {
        return text;
    }


    public String getValue() {
        return value;
    }


}
