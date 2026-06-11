package dev.xing.infinitechat.momentservice.model.vo;

import lombok.Data;
import java.io.Serializable;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeleteCommentVO implements Serializable {

    private Long commentId;

    private Long momentId;

    private Long userId;
}
