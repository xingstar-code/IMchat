package dev.xing.infinitechat.messagingservice.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xing.infinitechat.messagingservice.mapper.UserMapper;
import dev.xing.infinitechat.messagingservice.model.entity.User;
import dev.xing.infinitechat.messagingservice.service.UserService;
import org.springframework.stereotype.Service;

/**
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2024-10-17 14:21:28
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

}




