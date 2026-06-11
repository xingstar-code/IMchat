package dev.xing.infinitechat.ContactService.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.xing.infinitechat.ContactService.model.dto.FriendDTO;
import dev.xing.infinitechat.ContactService.model.entity.Friend;
import dev.xing.infinitechat.ContactService.model.dto.ModifyFriendApplicationResponse;
import dev.xing.infinitechat.ContactService.model.entity.User;
import dev.xing.infinitechat.ContactService.model.vo.FriendDetailVO;

public interface FriendService extends IService<Friend> {
    /**
     * 获取联系人列表
     *
     * @param userUuid 用户 UUID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param key      查询关键字
     * @return 分页后的好友列表
     */
    IPage<FriendDTO> getFriends(Long userUuid, int pageNum, int pageSize, String key);

    /**
     * 删除好友
     *
     * @param userId        用户id
     * @param friendId      好友id
     * @return 是否成功
     */
    boolean deleteFriend(Long userId, Long friendId);



    /**
     * 拉黑好友
     * @param userId 用户 uuid
     * @param friendId 被拉黑者用户 uuid
     */
    boolean blockFriend(Long userId, Long friendId);

    /**
     * 获取好友信息详情
     * @param userUuid 用户 uuid
     * @param friendUuid 好友 uuid
     * @return FriendDetailVO
     */
    FriendDetailVO getFriendDetails(String userUuid, String friendUuid);


    /**
     * 搜索用户
     * @param userId 用户id
     * @param key 手机号
     * @return
     */
    FriendDetailVO getUserDetails(String userId,String key);

    /**
     * 添加好友关系
     *
     * @param recipient   用户
     * @param friendId 好友ID
     */
    public ModifyFriendApplicationResponse addFriend(User recipient, Long friendId);


}