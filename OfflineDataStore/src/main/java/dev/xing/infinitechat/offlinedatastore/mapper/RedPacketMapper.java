package dev.xing.infinitechat.offlinedatastore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.xing.infinitechat.offlinedatastore.model.entity.RedPacket;


import org.apache.ibatis.annotations.Mapper;

/**
 * 红包主表 Mapper 接口
 */
@Mapper
public interface RedPacketMapper extends BaseMapper<RedPacket> {
}