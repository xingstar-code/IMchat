package dev.xing.infinitechat.offlinedatastore.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OfflineMsgResponse implements Serializable {

    private List<OfflineMsg> offlineMsg;
}
