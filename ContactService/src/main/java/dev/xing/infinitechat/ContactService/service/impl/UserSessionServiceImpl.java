package dev.xing.infinitechat.ContactService.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xing.infinitechat.ContactService.mapper.UserSessionMapper;
import dev.xing.infinitechat.ContactService.model.entity.UserSession;
import dev.xing.infinitechat.ContactService.service.UserSessionService;
import org.springframework.stereotype.Service;

@Service
public class UserSessionServiceImpl extends ServiceImpl<UserSessionMapper, UserSession> implements UserSessionService {

}