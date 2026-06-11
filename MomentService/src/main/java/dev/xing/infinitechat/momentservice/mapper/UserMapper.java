package dev.xing.infinitechat.momentservice.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.xing.infinitechat.momentservice.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
* @description 针对表【user(用户表)】的数据库操作Mapper
* @createDate 2024-10-08 16:08:49
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




