package dev.xing.infinitechat.authenticationservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.xing.infinitechat.authenticationservice.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
* @description 针对表【user(用户表)】的数据库操作Mapper
* @createDate 2024-08-04 21:20:40
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




