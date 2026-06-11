package dev.xing.infinitechat.messagingservice.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SendRedPacketRequest {

    private Long sessionId;

    private Long receiveUserId;

    private Long sendUserId;

    private Integer type;

    private Integer sessionType;

    private Body body;

    @Data
    @Accessors(chain = true)
    public static class Body {

        private Integer redPacketType;

        private BigDecimal totalAmount;

        private Integer totalCount;

        private String redPacketWrapperText;
    }
}
