package dev.xing.infinitechat.momentservice.model.vo;

import lombok.Data;
import java.io.Serializable;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CreateCommentVO implements Serializable {

    private Long momentId;

    private Long commentId;

    private Long parentCommentId;

    private String userName;

    private String comment;

    private String parentUserName;
}
