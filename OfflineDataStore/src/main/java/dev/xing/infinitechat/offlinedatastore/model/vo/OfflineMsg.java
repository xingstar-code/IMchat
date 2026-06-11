package dev.xing.infinitechat.offlinedatastore.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OfflineMsg implements Serializable {

    private Long total;

    private String sessionId;

    private String sessionName;

    private String sessionAvatar;

    private Integer sessionType;

    private List<OfflineMsgDetail> offlineMsgDetails;
}
