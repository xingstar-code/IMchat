package dev.xing.infinitechat.ContactService.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;
import lombok.experimental.Accessors;

@Data
@TableName("session")
@Accessors(chain = true)
public class Session {

    @TableId
    private Long id;

    private String name;

    private Integer type;

    private Integer status;

    private Date createdAt;

    private Date updatedAt;
}
