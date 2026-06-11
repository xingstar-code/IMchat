package dev.xing.infinitechat.offlinedatastore.mapper;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.xing.infinitechat.offlinedatastore.model.entity.Session;

import org.apache.ibatis.annotations.Mapper;

/**
* @description 针对表【session(会话表)】的数据库操作Mapper
* @createDate 2024-11-04 17:54:30
* @Entity generator.domain.Session
*/
@Mapper
public interface SessionMapper extends BaseMapper<Session> {

}




