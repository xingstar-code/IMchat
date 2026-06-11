package dev.xing.infinitechat.messagingservice.model.vo;

import java.util.Date;
import com.alibaba.fastjson.annotation.JSONType;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class KafkaMsgVO {

    protected Long sessionId;

    protected Long sendUserId;

    protected Long messageId;

    protected Integer sessionType;

    protected Integer type;

    protected String messageUuid;

    protected Date createAt;

    protected Object body;
}
