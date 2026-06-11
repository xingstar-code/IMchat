package dev.xing.infinitechat.momentservice.controller;

import dev.xing.infinitechat.momentservice.common.Result;
import dev.xing.infinitechat.momentservice.common.ResultGenerator;
import dev.xing.infinitechat.momentservice.model.dto.CreateMomentDTO;
import dev.xing.infinitechat.momentservice.model.dto.MomentCommentDTO;
import dev.xing.infinitechat.momentservice.model.entity.User;
import dev.xing.infinitechat.momentservice.model.enums.NoticeMomentEnum;
import dev.xing.infinitechat.momentservice.model.vo.*;
import dev.xing.infinitechat.momentservice.service.*;
import dev.xing.infinitechat.momentservice.utlis.SendOkHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api")
@SuppressWarnings({"all"})
public class MomentController {


    @Autowired
    private MomentService momentService;


    @Autowired
    private MomentLikeService momentLikeService;


    @Autowired
    private FriendService friendService;


    @Autowired
    private MomentCommentService momentCommentService;


    @Autowired
    private SendOkHttpRequest sendOkHttpRequest;


    @Autowired
    private UserService userService;


    @PostMapping("/v1/moment/saveMomentMedias")
    public Result saveMomentMedias(@RequestParam("files") MultipartFile[] files) throws Exception {
        List<String> urls = momentService.saveMomentMedias(files);
        return ResultGenerator.genSuccessResult(urls);
    }
    // 创建朋友圈
    @PostMapping("/v1/moment")
    public Result createMoment(@RequestBody CreateMomentDTO createMomentDTO) throws Exception {
        log.info("createMomentDTO:{}", createMomentDTO);
        //保存朋友圈
        MomentVO momentVO = momentService.saveMoment(Long.valueOf(createMomentDTO.getUserId()), createMomentDTO.getText(), createMomentDTO.getMediaUrls());
        // 发送消息通知, 获取 Netty 服务器地址, 推送通知
        MomentRTCVO momentRTCVO = new MomentRTCVO();
        momentRTCVO.setNoticeType(NoticeMomentEnum.CREATE_MOMENT_NOTICE.getValue());
        User user = userService.getById(Long.valueOf(createMomentDTO.getUserId()));
        momentRTCVO.setAvatar(user.getAvatar());
        sendOkHttpRequest.sendOkHttp(momentRTCVO, Long.valueOf(createMomentDTO.getUserId()), NoticeMomentEnum.CREATE_MOMENT_NOTICE.getValue(), momentVO.getMomentId());
        return ResultGenerator.genSuccessResult(momentVO);
    }

    // 获取朋友朋友圈列表
    @GetMapping("/v1/moment/list/{userId}")
    public Result getMoments(@PathVariable Long userId, @RequestParam String time) {
        // 获取好友 userId
        List<Long> friendIds = friendService.getFriendIds(userId);
        friendIds.add(userId);
        // 获取好友朋友圈以及详情
        MomentsListVO moments = momentService.getMoments(friendIds, time);
        return ResultGenerator.genSuccessResult(moments);
    }


    // 获取自己朋友圈列表
    @GetMapping("/v1/moment/list/self/{userId}")
    public Result getSelfMoments(@PathVariable Long userId, @RequestParam String time) {
        List<Long> user = new ArrayList<>();
        user.add(userId);
        MomentsListVO moments = momentService.getMoments(user, time);
        return ResultGenerator.genSuccessResult(moments);
    }


    // 删除朋友圈
    @DeleteMapping("/v1/moment/{momentId}")
    public Result deleteMoment(@PathVariable Long momentId, @RequestParam Long userId) throws Exception {
        boolean result = momentService.deleteMoment(momentId, userId);
        if (!result) {
            return ResultGenerator.genFailResult("删除失败, 朋友圈不存在");
        }
        return ResultGenerator.genSuccessResult("朋友圈删除成功！");
    }


    // 点赞
    @GetMapping("/v1/moment/like/{momentId}")
    public Result likeMoment(@PathVariable Long momentId, @RequestParam Long userId) throws Exception {
        Long likeId = momentLikeService.createLike(momentId, userId);
        // 发送消息通知, 获取 Netty 服务器地址, 推送通知
        MomentRTCVO momentRTCVO = new MomentRTCVO();
        momentRTCVO.setNoticeType(NoticeMomentEnum.CREATE_MOMENT_COMMENT_LIKE_NOTICE.getValue());
        sendOkHttpRequest.sendOkHttp(momentRTCVO, userId, NoticeMomentEnum.CREATE_MOMENT_COMMENT_LIKE_NOTICE.getValue(), momentId);
        return ResultGenerator.genSuccessResult(likeId);
    }


    // 取消点赞
    @DeleteMapping("/v1/moment/like/{momentId}")
    public Result deleteLikeMoment(@PathVariable Long momentId, @RequestParam Long likeId, @RequestParam Long userId) throws Exception {
        boolean result = momentLikeService.deleteLike(momentId, likeId, userId);
        if (!result) {
            return ResultGenerator.genFailResult("取消点赞失败, 点赞不存在");
        }
        return ResultGenerator.genSuccessResult("取消点赞成功！");
    }


    // 评论
    @PostMapping("/v1/moment/comment/{momentId}")
    public Result createCommentComent(@PathVariable Long momentId, @RequestBody MomentCommentDTO momentCommentDTO) throws Exception {
        MomentCommentVO momentCommentVO = momentCommentService.createComment(momentId, momentCommentDTO);
        // 发送消息通知, 获取 Netty 服务器地址, 推送通知
        MomentRTCVO momentRTCVO = new MomentRTCVO();
        momentRTCVO.setNoticeType(NoticeMomentEnum.CREATE_MOMENT_COMMENT_LIKE_NOTICE.getValue());
        sendOkHttpRequest.sendOkHttp(momentRTCVO, momentCommentDTO.getUserId(), NoticeMomentEnum.CREATE_MOMENT_COMMENT_LIKE_NOTICE.getValue(), momentId);
        return ResultGenerator.genSuccessResult(momentCommentVO);
    }


    // 删除评论
    @DeleteMapping("/v1/moment/comment/{momentId}")
    public Result deleteComment(@PathVariable Long momentId, @RequestParam Long commentId, @RequestParam Long userId) throws Exception {
        boolean result = momentCommentService.deleteComment(momentId, commentId, userId);
        if (!result) {
            return ResultGenerator.genFailResult("删除失败, 评论不存在");
        }
        return ResultGenerator.genSuccessResult("评论删除成功！");
    }



    // 获取离线通知
    @GetMapping("/v1/moment/offline/notice/{userId}")
    public Result getOfflineNotice(@PathVariable Long userId, @RequestParam String time) {
        List<MomentOfflineVO> momentOfflineVOList = momentService.getOfflineNotice(userId, time);
        return ResultGenerator.genSuccessResult(momentOfflineVOList);
    }
}
