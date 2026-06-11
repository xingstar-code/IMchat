package dev.xing.infinitechat.momentservice.model.vo;

import lombok.Data;
import java.io.Serializable;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CreateLikeVO implements Serializable {

    private Long momentId;

    private Long userId;

    private Long likeId;

    private String userName;
}
