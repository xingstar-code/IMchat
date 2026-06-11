package dev.xing.infinitechat.handler;


import java.util.LinkedHashMap;
import java.util.Map;

import dev.xing.infinitechat.common.Result;
import dev.xing.infinitechat.common.ResultGenerator;
import dev.xing.infinitechat.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {



    @ResponseBody
    @ExceptionHandler(BusinessException.class)
    public Result<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultGenerator.genFailResult(e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(RuntimeException.class)
    public Result<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);

        String msg = e.getMessage();
        Throwable cause = e.getCause();
        log.error("系统错误:" + msg);
        return ResultGenerator.genFailResult(cause.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = IllegalArgumentException.class)
    public Result<?>  handlerIllegalArgumentException(Exception e) {
        IllegalArgumentException illegalArgumentException = (IllegalArgumentException) e;
        String msg = illegalArgumentException.getMessage();
        Throwable cause = illegalArgumentException.getCause();
        log.error("非法参数:" + msg);
        return ResultGenerator.genFailResult(msg + cause);
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("参数校验异常:{}", errors);
        return ResultGenerator.genFailResult(errors.toString());

    }



    @ResponseBody
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public Result<?> handlerMissingServletRequestParameterException(Exception e) {
        log.error("缺少必填参数:{}", e.toString());
        return ResultGenerator.genFailResult("缺少必填参数");
    }

    @ResponseBody
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public Result<?> handlerHttpMessageNotReadableException(Exception e) {
        log.error("请求参数异常:{}",e.toString());
        return ResultGenerator.genFailResult("请求参数异常");
    }

}
