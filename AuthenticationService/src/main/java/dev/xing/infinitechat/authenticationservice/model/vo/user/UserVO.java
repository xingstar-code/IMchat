package dev.xing.infinitechat.authenticationservice.model.vo.user;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class UserVO implements Serializable {

    /**
     * id
     */
    private String userId;

    /**
     * 用户昵称
     */
    private String userName;


    /**
     * 用户头像
     */
    private String avatar;


    /**
     * 个性签名
     */
    private String signature;


    /**
     * 性别 0 男 1 女
     */
    private Integer gender;



    private Integer status;

    private String nettyUrl;

    /**
     * 用户token
     */
    private String token;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
