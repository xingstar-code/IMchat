package dev.xing.infinitechat.offlinedatastore.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.xing.infinitechat.offlinedatastore.model.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
* @description 针对表【message】的数据库操作Mapper
* @createDate 2024-09-20 16:39:30
* @Entity generator.domain.Message
*/
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

}




