package dev.xing.infinitechat.exception;


import dev.xing.infinitechat.common.ErrorEnum;

/**
 * 自定义异常类
 *
 */
public class BusinessException extends RuntimeException {


    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(ErrorEnum errorEnum) {
        super(errorEnum.getMessage());
    }

    public BusinessException(ErrorEnum errorEnum, String message) {
        super(message);
    }

}
