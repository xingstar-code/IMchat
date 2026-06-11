package dev.xing.infinitechat.ContactService.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.xing.infinitechat.ContactService.model.entity.UserBalance;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户余额表 Mapper 接口
 */
@Mapper
public interface UserBalanceMapper extends BaseMapper<UserBalance> {
}