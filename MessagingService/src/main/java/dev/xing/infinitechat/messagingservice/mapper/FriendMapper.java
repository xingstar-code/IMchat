package dev.xing.infinitechat.messagingservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.xing.infinitechat.messagingservice.model.entity.Friend;
import org.apache.ibatis.annotations.*;

@Mapper
public interface FriendMapper extends BaseMapper<Friend> {
    /**
     * 检查好友关系
     * @param userId 用户id
     * @param friendId 朋友id
     * @return
     */
    @Select("SELECT * FROM friend WHERE user_id = #{userId} AND friend_id = #{friendId} AND status = 1")
    Friend selectFriendship(@Param("userId") Long userId, @Param("friendId") Long friendId);
}
