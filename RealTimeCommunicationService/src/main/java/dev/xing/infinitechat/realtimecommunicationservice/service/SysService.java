package dev.xing.infinitechat.realtimecommunicationservice.service;

import dev.xing.infinitechat.realtimecommunicationservice.module.vo.sys.OnlineUserVo;

/**
 * @InterfaceName SysService
 * @Description 系统接口服务
 * @Date 2024/11/23 17:08
 */
public interface SysService {
    OnlineUserVo getOnlineUser();
}
