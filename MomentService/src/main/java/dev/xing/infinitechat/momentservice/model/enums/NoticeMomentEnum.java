package dev.xing.infinitechat.momentservice.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum NoticeMomentEnum {

    CREATE_MOMENT_NOTICE("发布朋友圈",1),
    CREATE_MOMENT_COMMENT_LIKE_NOTICE("点赞评论朋友圈",2),
    MOMENT_NOTICE("朋友圈通知",3);

    private final String text;

    private final Integer value;

    NoticeMomentEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }


    public static List<Integer> getValues() {
        return Arrays.stream(NoticeMomentEnum.values()).map(NoticeMomentEnum::getValue).collect(Collectors.toList());
    }


    public static NoticeMomentEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (NoticeMomentEnum anEnum : NoticeMomentEnum.values()) {
            if (anEnum.getValue().equals(value)) {
                return anEnum;
            }

        }
        return null;
    }
    public String getText() {
        return text;
    }


    public Integer getValue() {
        return value;
    }

}
