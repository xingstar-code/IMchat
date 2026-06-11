package dev.xing.infinitechat.momentservice.model.vo;

import lombok.Data;
import java.io.Serializable;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeleteLikeVO implements Serializable {

    private Long momentId;

    private Long userId;

    private Long likeId;
}
