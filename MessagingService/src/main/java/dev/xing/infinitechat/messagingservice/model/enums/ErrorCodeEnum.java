package dev.xing.infinitechat.messagingservice.model.enums;

public enum ErrorCodeEnum {
    SUCCESS("ok"),
    PARAMS_ERROR("请求参数错误"),
    NOT_LOGIN_ERROR("未登录"),
    NO_AUTH_ERROR("无权限"),
    NOT_FOUND_ERROR("请求数据不存在"),
    FORBIDDEN_ERROR("禁止访问"),
    SYSTEM_ERROR("系统内部异常"),
    OPERATION_ERROR("操作失败"),
    CODE_ERROR("验证码错误"),
    NOT_REGISTER_ERROR("未注册, 用户不存在"),
    REGISTER_ERROR("注册失败, 用户已存在"),
    RESET_PWD_ERROR("重置密码失败, 用户不存在"),
    PASSWORD_ERROR("密码错误"),
    LOGIN_ERROR("登录失败, 用户名或密码错误"),
    SERVICE_ERROR("服务异常");
    /**
     * 状态码
     */

    /**
     * 信息
     */
    private final String message;

    ErrorCodeEnum(String message) {
        this.message = message;
    }


    public String getMessage() {
        return message;
    }
}
