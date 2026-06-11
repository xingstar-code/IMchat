package dev.xing.infinitechat.messagingservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.xing.infinitechat.messagingservice.model.entity.Message;

import org.apache.ibatis.annotations.Mapper;

/**
* @description 针对表【message】的数据库操作Mapper
* @createDate 2024-11-11 14:37:36
* @Entity generator.domain.Message
*/
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

}




