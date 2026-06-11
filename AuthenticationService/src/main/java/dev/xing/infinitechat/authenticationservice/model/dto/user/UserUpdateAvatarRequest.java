package dev.xing.infinitechat.authenticationservice.model.dto.user;

import javax.validation.constraints.NotEmpty;

/**
 * @ClassName UserUpdateAvatarRequest
 * @Description 更新用户头像请求体
 * @Date 2025/1/12 01:50
 */

public class UserUpdateAvatarRequest {
    // AvatarUrl 头像地址
    @NotEmpty(message = "头像地址不能为空")
    public String avatarUrl;

}
