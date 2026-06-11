package dev.xing.infinitechat.ContactService.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.xing.infinitechat.ContactService.model.dto.FriendDTO;
import dev.xing.infinitechat.ContactService.model.entity.Friend;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FriendMapper extends BaseMapper<Friend> {

    /**
     * 根据用户 ID 和关键字查找好友，支持分页
     *
     * @param userId 用户 ID
     * @param key    查询关键字
     * @return 好友列表
     */
    Page<FriendDTO> findFriendsByUserId(@Param("page") Page<FriendDTO> page,
                                        @Param("userId") Long userId,
                                        @Param("key") String key);



    /**
     * 得到用户所有正常朋友的id
     * @param userId
     * @return
     */
    @Select("SELECT friend_id FROM friend WHERE user_id = #{userId} AND status = 1")
    List<Long> getFriendIds(@Param("userId") Long userId);


    /**
     * 根据用户ID和离线时间查询好友列表
     *
     * @param userId      用户ID
     * @param offlineTime 用户离线时间
     * @return 好友列表
     */
    @Select("SELECT * FROM friend " +
            "WHERE user_id = #{userId} " +
            "AND status = 1 " +
            "AND created_at >= #{offlineTime}")
    List<Friend> findFriendsByUserIdAndTime(@Param("userId") Long userId,
                                            @Param("offlineTime") LocalDateTime offlineTime);
}
