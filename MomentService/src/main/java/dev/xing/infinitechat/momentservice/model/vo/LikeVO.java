package dev.xing.infinitechat.momentservice.model.vo;

import lombok.Data;
import java.io.Serializable;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LikeVO implements Serializable {

    private Long LikeId;

    private Long userId;

    private String userName;
}
