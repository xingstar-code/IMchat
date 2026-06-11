package dev.xing.infinitechat.messagingservice.model.entity;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@JSONType(orders = { "type", "data" })
@Accessors(chain = true)
public class MessageDTO {

    private Integer type;

    private Object data;
}
