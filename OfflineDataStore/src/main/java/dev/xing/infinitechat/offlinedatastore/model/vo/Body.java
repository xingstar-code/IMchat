package dev.xing.infinitechat.offlinedatastore.model.vo;

import lombok.Data;
import java.io.Serializable;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Body implements Serializable {

    private String content;

    private String createdAt;

    private String replyId;
}
