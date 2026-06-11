package dev.xing.infinitechat.authenticationservice.controller;

import javax.servlet.http.HttpServletRequest;

import dev.xing.infinitechat.authenticationservice.common.Result;
import dev.xing.infinitechat.authenticationservice.common.ResultGenerator;
import dev.xing.infinitechat.authenticationservice.model.dto.user.*;
import dev.xing.infinitechat.authenticationservice.model.enums.ConfigEnum;
import dev.xing.infinitechat.authenticationservice.model.enums.SuccessEnum;
import dev.xing.infinitechat.authenticationservice.model.enums.TimeOutEnum;
import dev.xing.infinitechat.authenticationservice.model.vo.user.UploadUrlResponse;
import dev.xing.infinitechat.authenticationservice.model.vo.user.UserVO;
import dev.xing.infinitechat.authenticationservice.service.UserService;
import dev.xing.infinitechat.authenticationservice.utils.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@Slf4j
@RestController
@RequestMapping("/api")
@SuppressWarnings({"all"})
public class UserController {



    @Autowired
    private UserService userService;



    @Autowired
    private UpdateAvatarUtil updateAvatarUtil;

    /**
       * @MethodName sendClientSms
       * @Description
       * @param: userSMSRequest
       * @Date 2025/1/9 11:21
       */
    @PostMapping("/v1/user/noToken/sms")
    public Result sendClientSms(@Valid @RequestBody UserSMSRequest userSMSRequest) throws Exception {
        userService.sendClientSms(userSMSRequest);
        return ResultGenerator.genSuccessResult(SuccessEnum.SUCCESS_CODE_SEND.getMessage());
    }

    /**
       * @MethodName register
       * @Description
       * @param: userRegisterRequest
       * @Date 2025/1/9 11:21
       */
    @PostMapping("/v1/user/noToken/register")
    public Result register(@Valid @RequestBody UserRegisterRequest userRegisterRequest) throws Exception {
        userService.userRegister(userRegisterRequest);
        return ResultGenerator.genSuccessResult(SuccessEnum.SUCCESS_REGISTER.getMessage());
    }

    /**
       * @MethodName loginPwd
       * @Description
       * @param: userLoginPwdRequest
       * @param: response
       * @Date 2025/1/9 11:21
       */
    @PostMapping("/v1/user/noToken/loginPwd")
    public Result loginPwd(@Valid @RequestBody UserLoginPwdRequest userLoginPwdRequest, HttpServletResponse response) throws Exception {
        UserVO userVO = userService.userLoginPwd(userLoginPwdRequest, response);
        return ResultGenerator.genSuccessResult(userVO);
    }

    /**
       * @MethodName loginCode
       * @Description
       * @param: userLoginCodeRequest
       * @param: response
       * @Date 2025/1/9 11:21
       */
    @PostMapping("/v1/user/noToken/loginCode")
    public Result loginCode(@Valid @RequestBody UserLoginCodeRequest userLoginCodeRequest, HttpServletResponse response) throws Exception {
        UserVO userVO = userService.userLoginCode(userLoginCodeRequest, response);
        return ResultGenerator.genSuccessResult(userVO);
    }


    /**
       * @MethodName Logout
       * @Description
       * @param: userLogOutRequest
       * @Date 2025/1/9 11:21
       */
    @PostMapping("/v1/user/logout")
    public Result Logout(@Valid @RequestBody UserLogOutRequest userLogOutRequest) {
        userService.userLogout(userLogOutRequest);
        return ResultGenerator.genSuccessResult(SuccessEnum.SUCCESS_LOGOUT.getMessage());
    }


    /**
       * @MethodName updateAvatar
       * @Description
       * @param: file
       * @param: request
       * @Date 2025/1/9 11:20
       */
    @PatchMapping("/v1/user/avatar")
    public Result updateAvatar(@Valid @RequestBody UserUpdateAvatarRequest request, HttpServletRequest httpServletRequest) throws Exception {
        String id = JwtUtil.parse(httpServletRequest.getHeader(ConfigEnum.AUTHORIZATION.getValue())).getSubject();
        userService.updateAvatar(id, request);
        return ResultGenerator.genSuccessResult(null);
    }

    /***
     * @MethodName getUploadUrl
     * @Description 获取上传文件签名地址
     * @param: fileName
     * @return: dev.xing.infinitechat.authenticationservice.common.Result
     * @Date 2025/1/11 23:57
     */
    @GetMapping("v1/user/uploadUrl")
    public Result getUploadUrl(@Valid @NotEmpty(message = "文件名称不能为空") @RequestParam("fileName") String fileName) throws Exception {
        UploadUrlResponse response = userService.getUploadUrl(fileName);
        return ResultGenerator.genSuccessResult(response);
    }

}
