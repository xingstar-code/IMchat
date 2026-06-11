package dev.xing.infinitechat.offlinedatastore.controller;

import dev.xing.infinitechat.offlinedatastore.common.Result;
import dev.xing.infinitechat.offlinedatastore.common.ResultGenerator;

import dev.xing.infinitechat.offlinedatastore.model.entity.Session;
import dev.xing.infinitechat.offlinedatastore.model.vo.OfflineMsg;
import dev.xing.infinitechat.offlinedatastore.model.vo.OfflineMsgDetail;
import dev.xing.infinitechat.offlinedatastore.model.vo.OfflineMsgResponse;
import dev.xing.infinitechat.offlinedatastore.service.MessageService;
import dev.xing.infinitechat.offlinedatastore.service.SessionService;
import dev.xing.infinitechat.offlinedatastore.service.UserSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api")
@SuppressWarnings({"all"})
public class SendOfflineMsgController {


    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private SessionService sessionService;

    @GetMapping("/v1/offline/message")
    public Result sendOfflineMsg(@RequestParam Long userId, @RequestParam String time) {
        //  根据 userId 查找用户的会话
        Set<Long> sessionIds = userSessionService.findSessionIdByUserId(userId);
        log.info("sessionIds:{}", sessionIds);
        // 根据所有的 sessionId 查找到所有的离线消息
        OfflineMsgResponse offlineMsgResponses = new OfflineMsgResponse();
        List<OfflineMsg> offlineMsgs = new ArrayList<>();
        for (Long sessionId : sessionIds) {
            Session session = sessionService.getById(sessionId);
            OfflineMsg offlineMsg = new OfflineMsg();
            offlineMsg.setSessionId(sessionId.toString());
            offlineMsg.setSessionType(session.getType());
            if (session.getType() == 2) {
                offlineMsg.setSessionAvatar("http://localhost:8080/img/avatar/IM_GROUP.jpg");
                offlineMsg.setSessionName(session.getName());
            }
            List<OfflineMsgDetail> offlineMsgDetails = messageService.findOfflineMsgBySessionId(sessionId, time);
            offlineMsg.setOfflineMsgDetails(offlineMsgDetails);
            offlineMsg.setTotal((long) offlineMsgDetails.size());
            if (offlineMsgDetails.size() > 0) {
                offlineMsgs.add(offlineMsg);
            }
        }
        // 将离线消息发送给用户
        offlineMsgResponses.setOfflineMsg(offlineMsgs);
        return ResultGenerator.genSuccessResult(offlineMsgResponses);
    }
}
