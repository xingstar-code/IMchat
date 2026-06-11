package dev.xing.infinitechat.momentservice.model.entity;

import lombok.Data;
import java.io.Serializable;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Comment implements Serializable {

    private String parentUserName;

    private String userName;

    private String comment;

    private Long commentId;

    private Long parentCommentId;
}
