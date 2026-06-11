package dev.xing.infinitechat.realtimecommunicationservice.controller;

import dev.xing.infinitechat.realtimecommunicationservice.common.Result;
import dev.xing.infinitechat.realtimecommunicationservice.common.ResultGenerator;
import dev.xing.infinitechat.realtimecommunicationservice.module.dto.push.FriendApplicationNotification;
import dev.xing.infinitechat.realtimecommunicationservice.module.dto.push.MomentNotification;
import dev.xing.infinitechat.realtimecommunicationservice.module.dto.push.NewGroupSessionNotification;
import dev.xing.infinitechat.realtimecommunicationservice.module.dto.push.NewSessionNotification;
import dev.xing.infinitechat.realtimecommunicationservice.service.NettyMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1/message/push")
public class PushController {

    @Autowired
    private NettyMessageService nettyMessageService;

    /**
     * 发送新会话通知
     *
     * @param notification    新会话通知对象
     * @param userId 接收通知的用户ID
     */
    @PostMapping("/newSession/{userId}")
    public Result pushNewSession(
            @PathVariable("userId") String userId,
            @RequestBody NewSessionNotification notification
    ) {
        nettyMessageService.sendNewSessionNotification(notification, userId);
        return ResultGenerator.genSuccessResult("New session notification pushed.");
    }

    /**
     * 发送好友申请通知
     *
     * @param notification    好友申请通知对象
     * @param userId 接收通知的用户ID
     */
    @PostMapping("/friendApplication/{userId}")
    public Result pushFriendApplication(
            @PathVariable("userId") String userId,
            @RequestBody FriendApplicationNotification notification
    ) {
        nettyMessageService.sendFriendApplicationNotification(notification, userId);
        return ResultGenerator.genSuccessResult("Friend application notification pushed.");
    }



    /**
     * 发送新群会话通知
     *
     * @param notification    新群会话通知对象
     * @param userId 接收通知的用户ID
     */
    @PostMapping("/newGroupSession/{userId}")
    public Result pushNewGroupSession(
            @PathVariable("userId") String userId,
            @RequestBody NewGroupSessionNotification notification
    ) {
        nettyMessageService.sendNewGroupSessionNotification(notification, userId);
        return ResultGenerator.genSuccessResult("New Group session notification pushed.");
    }






    /**
     * 推送朋友圈通知
     *
     * @return
     */

    @PostMapping("/moment")
    public Result receiveNoticeMoment(@RequestBody MomentNotification momentNotification){
        nettyMessageService.sendNoticeMoment(momentNotification);
        return ResultGenerator.genSuccessResult();
    }
}
