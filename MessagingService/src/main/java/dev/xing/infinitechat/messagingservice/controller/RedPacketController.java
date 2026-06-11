package dev.xing.infinitechat.messagingservice.controller;

import dev.xing.infinitechat.messagingservice.common.Result;
import dev.xing.infinitechat.messagingservice.common.ResultGenerator;
import dev.xing.infinitechat.messagingservice.model.dto.*;
import dev.xing.infinitechat.messagingservice.model.vo.ResponseMsgVo;
import dev.xing.infinitechat.messagingservice.service.GetRedPacketService;
import dev.xing.infinitechat.messagingservice.service.RedPacketReceiveService;
import dev.xing.infinitechat.messagingservice.service.RedPacketService;
import dev.xing.infinitechat.messagingservice.utils.PreventDuplicateSubmit;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat/redPacket")
public class RedPacketController {

    @Autowired
    private RedPacketService redPacketService;

    @Autowired
    private RedPacketReceiveService redPacketReceiveService;

    @Autowired
    private GetRedPacketService getRedPacketService;

    /**
     * 发送红包
     */
    @SneakyThrows
    @PreventDuplicateSubmit // 防止重复提交
    @PostMapping("/send")
    public Result<ResponseMsgVo> sendRedPacket(@RequestBody SendRedPacketRequest request) {
        return ResultGenerator.genSuccessResult(redPacketService.sendRedPacket(request));
    }

    /**
     * 领取红包
     */
    @SneakyThrows
    @PostMapping("/receive")
    public Result<ReceiveRedPacketResponse> receiveRedPacket(@RequestBody ReceiveRedPacketRequest request) {
        return ResultGenerator.genSuccessResult(redPacketReceiveService.receiveRedPacket(request.getUserId(), request.getRedPacketId()));
    }


    /**
     * 查询单个红包领取记录
     *
     * @param redPacketId 红包ID
     * @param pageNum     页码，默认为1
     * @param pageSize    每页大小，默认为10
     * @return 响应结果
     */
    @GetMapping("/{redPacketId}")
    public Result<RedPacketResponse> getRedPacket(@PathVariable Long redPacketId,
                                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResultGenerator.genSuccessResult(getRedPacketService.getRedPacketDetails(redPacketId, pageNum, pageSize));
    }
}
