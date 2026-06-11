package dev.xing.infinitechat.ContactService.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
import lombok.experimental.Accessors;

@Data
@TableName("user")
@Accessors(chain = true)
public class User {

    @TableId
    private Long userId;

    private String userName;

    private String password;

    private String email;

    private String phone;

    private String avatar;

    private String signature;

    private Integer gender;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
