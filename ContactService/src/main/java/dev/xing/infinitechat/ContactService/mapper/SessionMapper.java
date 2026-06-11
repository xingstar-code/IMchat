package dev.xing.infinitechat.ContactService.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.xing.infinitechat.ContactService.model.dto.GroupMemberDTO;
import dev.xing.infinitechat.ContactService.model.entity.Session;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SessionMapper extends BaseMapper<Session> {

    /**
     * 查找两个用户之间的单聊会话ID
     *
     * @param userId   用户ID
     * @param friendId 好友ID
     * @return 会话ID列表
     */
    @Select("SELECT s.id " +
            "FROM session s " +
            "JOIN user_session us1 ON s.id = us1.session_id AND us1.user_id = #{userId} " +
            "JOIN user_session us2 ON s.id = us2.session_id AND us2.user_id = #{friendId} " +
            "WHERE s.type = 1 AND s.status != 2")
    List<Long> selectSessionIdsBetweenUsers(Long userId, Long friendId);


    /**
     * 根据用户ID列表、会话类型和离线时间查询会话列表
     *
     * @param userId     用户ID
     * @param sessionType 会话类型（1 单聊，2 群聊）
     * @param offlineTime 用户离线时间
     * @return 会话列表
     */
    @Select({
            "<script>",
            "SELECT s.* FROM session s ",
            "JOIN user_session us ON s.id = us.session_id ",
            "WHERE us.user_id = #{userId}",
            "AND s.type = #{sessionType} ",
            "AND s.created_at >= #{offlineTime} ",
            "AND s.status = 1",
            "</script>"
    })
    List<Session> findSessionsByUserIdsAndType(@Param("userId") Long userId,
                                               @Param("sessionType") int sessionType,
                                               @Param("offlineTime") LocalDateTime offlineTime);

    /**
     * 根据会话ID列表查询会话信息
     *
     * @param sessionIds 会话ID列表
     * @return 会话列表
     */
    @Select({
            "<script>",
            "SELECT * FROM session ",
            "WHERE id IN ",
            "<foreach item='id' collection='sessionIds' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach> ",
            "AND status = 1",
            "</script>"
    })
    List<Session> findSessionsByIds(@Param("sessionIds") List<Long> sessionIds);



    /**
     * 根据会话ID查询会话信息
     *
     * @param sessionId 会话ID
     * @return 会话对象，如果不存在则返回 null
     */
    @Select("SELECT * FROM session WHERE id = #{sessionId} AND status = 1")
    Session selectSessionById(Long sessionId);

    /**
     * 根据会话ID查询群成员信息
     *
     * @param sessionId 会话ID
     * @return 群成员列表
     */
    @Select("SELECT u.user_id AS userId, u.user_name AS userName, u.avatar AS avatar " +
            "FROM user u " +
            "INNER JOIN user_session us ON u.user_id = us.user_id " +
            "WHERE us.session_id = #{sessionId} AND us.status = 1")
    List<GroupMemberDTO> selectGroupMembers(Long sessionId);


}