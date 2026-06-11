package dev.xing.infinitechat.offlinedatastore.model.entity;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@JSONType(orders = { "receiveUserId", "sendUserId", "sessionId", "sessionType", "type", "body" })
@Accessors(chain = true)
public class MessageDTO {

    protected Long receiveUserId;

    protected Long sendUserId;

    protected Long sessionId;

    protected Long messageId;

    protected Integer type;

    protected Integer sessionType;

    protected Object body;
}
