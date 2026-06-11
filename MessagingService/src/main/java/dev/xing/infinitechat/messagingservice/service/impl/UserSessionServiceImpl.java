package dev.xing.infinitechat.messagingservice.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xing.infinitechat.messagingservice.mapper.UserSessionMapper;
import dev.xing.infinitechat.messagingservice.model.entity.UserSession;
import dev.xing.infinitechat.messagingservice.service.UserSessionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @description 针对表【user_session】的数据库操作Service实现
* @createDate 2024-10-17 11:02:09
*/
@Service
public class UserSessionServiceImpl extends ServiceImpl<UserSessionMapper, UserSession>
    implements UserSessionService {

    @Override
    public List<Long> getUserIdsBySessionId(Long sessionId) {
        List<UserSession> userSessionList = this.lambdaQuery().eq(UserSession::getSessionId, sessionId).list();
        return userSessionList.stream().map(UserSession::getUserId).collect(Collectors.toList());

    }
}




