package dev.xing.infinitechat.momentservice.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MomentVO implements Serializable {

    private Long momentId;

    private Long userId;

    private String text;

    private List<String> mediaUrls;
}
