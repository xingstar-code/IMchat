package dev.xing.infinitechat.realtimecommunicationservice.module.dto.push;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MessageNotification {

    private String messageUuid;

    private String sendUserUuid;

    private Long sessionId;

    // 1文本，2图片，3文件，4视频，5红包，6表情包
    private Integer messageType;

    private MessageBody body;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    @Data
    @Accessors(chain = true)
    public static class MessageBody {

        private String content;

        // 可选，回复的消息ID
        private String replyUuid;
    }
}
