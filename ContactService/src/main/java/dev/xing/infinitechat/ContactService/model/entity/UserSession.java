package dev.xing.infinitechat.ContactService.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;
import lombok.experimental.Accessors;

@Data
@TableName("user_session")
@Accessors(chain = true)
public class UserSession {

    @TableId
    private Long id;

    private Long userId;

    private Long sessionId;

    private Integer role;

    private Integer status;

    private Date createdAt;

    private Date updatedAt;
}
