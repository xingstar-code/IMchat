package dev.xing.infinitechat.realtimecommunicationservice.controller;

import dev.xing.infinitechat.realtimecommunicationservice.common.Result;
import dev.xing.infinitechat.realtimecommunicationservice.common.ResultGenerator;
import dev.xing.infinitechat.realtimecommunicationservice.module.entity.Message;
import dev.xing.infinitechat.realtimecommunicationservice.service.NettyMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/message/user")
@Slf4j
@RequiredArgsConstructor
public class RcvMsgController {
    private final NettyMessageService nettyMessageService;

    @PostMapping
    public Result receiveMessage(@RequestBody Message message){
            log.info("message:{}",message);
            nettyMessageService.sendMessageToUser(message);
            return ResultGenerator.genSuccessResult();
    }
}
