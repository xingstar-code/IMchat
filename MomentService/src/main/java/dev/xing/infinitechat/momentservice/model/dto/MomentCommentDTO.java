package dev.xing.infinitechat.momentservice.model.dto;

import lombok.Data;
import java.io.Serializable;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MomentCommentDTO implements Serializable {

    private Long userId;

    private String comment;

    private Long parentCommentId;
}
