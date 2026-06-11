package dev.xing.infinitechat.ContactService.service;

import com.baomidou.mybatisplus.extension.service.IService;
import dev.xing.infinitechat.common.Result;
import dev.xing.infinitechat.ContactService.model.entity.ApplyFriend;
import dev.xing.infinitechat.ContactService.model.dto.ModifyFriendApplicationResponse;

public interface ApplyFriendService extends IService<ApplyFriend> {
    /**
     * 添加好友
     */
    boolean addFriend(String userUuid, String receiveUserUuid, String msg) throws Exception;

    /**
     * 获取申请添加好友列表的方法
     */
    Result getApplyList(Long userUuid, int pageNum, int pageSize, String key);

    /**
     * 获取用户的未读好友申请数量
     *
     * @param userId 用户ID
     * @return 未读好友申请数量
     */
    int getApplyCount(Long userId);


    /**
     * 修改好友申请状态
     *
     * @param userUuid         添加者用户 UUID
     * @param status           新的状态
     * @param receiveUserUuids 接收者用户 UUID 列表
     */
    ModifyFriendApplicationResponse modifyFriendApplicationStatus(String userUuid, Integer status, java.util.List<String> receiveUserUuids);


}