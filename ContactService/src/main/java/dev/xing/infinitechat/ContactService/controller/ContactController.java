package dev.xing.infinitechat.ContactService.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.xing.infinitechat.common.Result;
import dev.xing.infinitechat.common.ResultGenerator;
import dev.xing.infinitechat.common.ServiceException;
import dev.xing.infinitechat.ContactService.model.dto.*;
import dev.xing.infinitechat.ContactService.model.dto.push.OfflinePushResponse;
import dev.xing.infinitechat.ContactService.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/contact")
public class ContactController {

    @Autowired
    private FriendService friendService;

    @Autowired
    private ApplyFriendService applyFriendService;

    @Autowired
    private SessionService sessionService;

    /**
     * 通过手机号搜索用户
     *
     * @param userUuid 用户id
     * @param key      手机号
     * @return
     */
    @GetMapping("/{userUuid}/user")
    public Result getUser(
            @PathVariable("userUuid") String userUuid,
            @RequestParam(value = "phone", defaultValue = "") String key) {
        return ResultGenerator.genSuccessResult(friendService.getUserDetails(userUuid, key));
    }


    /**
     * 获取联系人列表
     *
     * @param userUuid 用户id
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param key      查询关键字
     * @return list 联系人列表
     */
    @GetMapping("/{userUuid}/friend")
    public Result getFriends(
            @PathVariable("userUuid") Long userUuid,
            @RequestParam(value = "pageNum", defaultValue = "0") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(value = "key", defaultValue = "") String key) {

        IPage<FriendDTO> friendsPage = friendService.getFriends(userUuid, pageNum, pageSize, key);
        Map<String, Object> data = new HashMap<>();
        data.put("list", friendsPage.getRecords());
        data.put("total", friendsPage.getTotal());
        return ResultGenerator.genSuccessResult(data);
    }


    /**
     * 添加好友
     *
     * @param userUuid        添加者用户 uuid
     * @param receiveUserUuid 接收者用户 uuid
     * @param request         申请信息
     * @return boolean
     */
    @PostMapping("/{userUuid}/friend/{receiveUserUuid}")
    public Result addFriend(
            @PathVariable("userUuid") String userUuid,
            @PathVariable("receiveUserUuid") String receiveUserUuid,
            @RequestBody AddFriendRequest request) throws Exception {
        return ResultGenerator.toResult(applyFriendService.addFriend(userUuid, receiveUserUuid, request.getMsg()));
    }


    /**
     * 获取添加好友请求列表
     *
     * @param userUuid 用户id
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param key      查询关键字
     * @return list 联系人列表
     */

    @GetMapping("/{userUuid}/apply")
    public Result getApplyList(
            @PathVariable Long userUuid,
            @RequestParam(required = false, defaultValue = "0") int pageNum,
            @RequestParam(required = false, defaultValue = "20") int pageSize,
            @RequestParam(required = false, defaultValue = "") String key) {
        return applyFriendService.getApplyList(userUuid, pageNum, pageSize, key);
    }


    /**
     * 获取未读好友申请数量
     *
     * @param userUuid 用户ID
     * @return 未读好友申请数量
     */
    @GetMapping("/{userUuid}/applyCount")
    public Result getUnreadApplyCount(@PathVariable Long userUuid) {
        if (userUuid == null) {
            throw new ServiceException("用户ID不能为空");
        }
        HashMap<String, Integer> map = new HashMap<>();
        map.put("count", applyFriendService.getApplyCount(userUuid));
        return ResultGenerator.genSuccessResult(map);
    }


    /**
     * 删除好友
     *
     * @param userUuid        用户id
     * @param receiveUserUuid 删除用户id
     * @return Result
     */
    @DeleteMapping("/{userUuid}/friend/{receiveUserUuid}")
    public Result deleteFriend(@PathVariable String userUuid, @PathVariable String receiveUserUuid) {
        return ResultGenerator.toResult(friendService.deleteFriend(Long.valueOf(userUuid), Long.valueOf(receiveUserUuid)));
    }


    /**
     * 拉黑好友
     *
     * @param userUuid        用户id
     * @param receiveUserUuid 拉黑用户id
     * @return Result
     */
    @PostMapping("/{userUuid}/block/{receiveUserUuid}")
    public Result blockFriend(@PathVariable String userUuid, @PathVariable String receiveUserUuid) {
        return ResultGenerator.toResult(friendService.blockFriend(Long.valueOf(userUuid), Long.valueOf(receiveUserUuid)));
    }


    /**
     * 修改好友申请状态
     *
     * @param userUuid 用户id
     * @param status   状态（审核通过1、拒绝2、已读3批量）
     * @param request  用户id列表
     * @return
     */
    @PostMapping("/{userUuid}/application/{status}")
    public Result<?> modifyFriendApplicationStatus(
            @PathVariable("userUuid") String userUuid,
            @PathVariable("status") Integer status,
            @RequestBody ModifyFriendApplicationRequest request) {
        try {
            List<String> receiveUserUuids = request.getReceiveUserUuids();

            // 调用服务层方法并获取响应数据
            ModifyFriendApplicationResponse response = applyFriendService.modifyFriendApplicationStatus(userUuid, status, receiveUserUuids);
            return ResultGenerator.genSuccessResult(response);
        } catch (ServiceException e) {
            return ResultGenerator.genFailResult(e.getMessage());
        } catch (Exception e) {
            System.out.println(e);
            return ResultGenerator.genFailResult("Internal server error");
        }
    }

    /**
     * 获取用户信息详情
     *
     * @param userUuid   用户id
     * @param friendUuid 好友 id
     * @return
     */
    @GetMapping("/{userUuid}/friend/{friendUuid}")
    public Result getFriendDetail(
            @PathVariable("userUuid") String userUuid,
            @PathVariable("friendUuid") String friendUuid) {
        // 获取好友详情
        return ResultGenerator.genSuccessResult(friendService.getFriendDetails(userUuid, friendUuid));
    }


    /**
     * 创建群聊
     *
     * @param request
     * @return
     */
    @PostMapping("/groups")
    public Result createGroup(@RequestBody CreateGroupRequest request) {
        return ResultGenerator.genSuccessResult(sessionService.createGroup(request));
    }


    @Autowired
    private GroupService groupService;

    /**
     * 群聊邀请接口
     *
     * @param inviteGroupRequest 群聊邀请请求参数
     * @return 邀请结果
     */
    @PostMapping("/group/invite")
    public Result<InviteGroupResponse> inviteGroup(@Valid @RequestBody InviteGroupRequest inviteGroupRequest) throws Exception {
        return ResultGenerator.genSuccessResult(groupService.inviteGroup(inviteGroupRequest));
    }


    @Autowired
    private KickGroupService kickGroupMembers;

    /**
     * 群聊踢人接口
     *
     * @param request 包含 sessionId、operatorId 和 memberIds 的请求体
     * @return 包含成功移出用户 ID 列表的响应结果
     */
    @PostMapping("/group/kick")
    public Result<KickGroupMembersResponse> kickGroupMembers(@Valid @RequestBody KickGroupMembersRequest request) {
        return ResultGenerator.genSuccessResult(kickGroupMembers.kickGroupMembers(request));
    }

    @Autowired
    private ExitGroupService exitGroupService;


    /**
     * 退出群聊接口
     *
     * @param groupExitRequestDTO 群聊退出请求DTO
     * @return 统一API响应结果
     */
    @PostMapping("/group/exit")
    public Result<?> exitGroup(@RequestBody GroupExitRequestDTO groupExitRequestDTO) {
        return ResultGenerator.toResult(exitGroupService.exitGroup(groupExitRequestDTO));
    }


    @Autowired
    private GetGroupMembersService getGroupMembersService;

    /**
     * 获取群聊内所有成员
     *
     * @param sessionId 群聊会话ID
     * @return 包含群成员信息的响应结果
     */
    @GetMapping("/group/{sessionId}/members")
    public Result<GroupMembersResponse> getGroupMembers(@PathVariable("sessionId") Long sessionId) {
        return ResultGenerator.genSuccessResult(getGroupMembersService.getGroupMembers(sessionId));
    }


    @Autowired
    private UserBalanceService userBalanceService;

    /**
     * 用户余额查询
     *
     * @param userId
     * @return
     */
    @GetMapping("/balance/{userId}")
    public Result<UserBalanceResponse> getUserBalance(@PathVariable Long userId) {
        return ResultGenerator.genSuccessResult(userBalanceService.getUserBalance(userId));
    }


    @Autowired
    private UserBalanceLogService userBalanceLogService;

    /**
     * 用户余额明细查询
     *
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/balanceDetail/{userId}")
    public Result<?> getBalanceDetail(
            @PathVariable Long userId,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {

        return ResultGenerator.genSuccessResult(userBalanceLogService.getBalanceDetail(userId, pageNum, pageSize));
    }

    @Autowired
    private OfflinePushService offlinePushService;

    /**
     * 获取离线推送消息
     *
     * @param userId      用户ID
     * @param offlineTime 用户离线时间，格式为：yyyy-MM-dd HH:mm:ss
     * @return 离线推送消息结果
     */
    @GetMapping("/offline/push")
    public Result<OfflinePushResponse> getOfflinePush(
            @RequestParam("userId") Long userId,
            @RequestParam("offlineTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime offlineTime) {
        return ResultGenerator.genSuccessResult(offlinePushService.getOfflinePush(userId, offlineTime));
    }

}