package dev.xing.infinitechat.ContactService.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.xing.infinitechat.ContactService.model.dto.BalanceLogDTO;
import dev.xing.infinitechat.ContactService.model.entity.BalanceLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 余额变动记录表 Mapper 接口
 */
@Mapper
public interface BalanceLogMapper extends BaseMapper<BalanceLog> {
    @Select("SELECT u.user_name AS userName, b.type, b.amount, DATE_FORMAT(b.created_at, '%m月%d日 %H:%i') AS time " +
            "FROM balance_log b " +
            "LEFT JOIN user u ON b.user_id = u.user_id " +
            "WHERE b.user_id = #{userId} " +
            "ORDER BY b.created_at DESC " +
            "LIMIT #{pageNum}, #{pageSize}")
    List<BalanceLogDTO> selectBalanceLogsByUserId(@Param("userId") Long userId, @Param("pageNum") Integer pageNum,
                                                  @Param("pageSize") Integer pageSize);
}