package dev.xing.infinitechat.realtimecommunicationservice.controller;

import dev.xing.infinitechat.realtimecommunicationservice.common.Result;
import dev.xing.infinitechat.realtimecommunicationservice.common.ResultGenerator;
import dev.xing.infinitechat.realtimecommunicationservice.module.vo.sys.OnlineUserVo;
import dev.xing.infinitechat.realtimecommunicationservice.service.SysService;
import dev.xing.infinitechat.realtimecommunicationservice.service.impl.SysServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @ClassName SysController
 * @Description 系统相关接口
 * @Date 2024/11/23 16:52
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/sys")
public class SysController {

    // sysService 系统服务
    @Resource
    private SysService sysService;

    /**
     * @MethodName getOnlineUser
     * @Description 获取在线用户信息
     * @return: Result<List<OnlineUserVo>>
     * @Date 2024/11/23 17:17
     */
    @GetMapping("/onlineUser")
    public Result<OnlineUserVo> getOnlineUser(){
        OnlineUserVo onlineUser = sysService.getOnlineUser();

        return ResultGenerator.genSuccessResult(onlineUser);
    }
}
