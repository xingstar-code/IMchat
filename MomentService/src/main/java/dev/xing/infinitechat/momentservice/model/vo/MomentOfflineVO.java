package dev.xing.infinitechat.momentservice.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MomentOfflineVO implements Serializable {

    private Integer type;

    private Integer noticeType;

    private String avatar;

    private Integer total;
}
