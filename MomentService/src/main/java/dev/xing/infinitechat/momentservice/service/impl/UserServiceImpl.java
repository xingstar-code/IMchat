package dev.xing.infinitechat.momentservice.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xing.infinitechat.momentservice.mapper.UserMapper;
import dev.xing.infinitechat.momentservice.model.entity.User;
import dev.xing.infinitechat.momentservice.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2024-10-08 16:08:49
 */
@Service
@SuppressWarnings({"all"})
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}




