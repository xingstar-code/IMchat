package dev.xing.infinitechat.realtimecommunicationservice.module.dto.push;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MomentNotification {

    private List<Long> receiveUserIds;

    private Integer noticeType;

    private String avatar;

    private Integer total;
}
