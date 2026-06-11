package dev.xing.infinitechat.offlinedatastore.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xing.infinitechat.offlinedatastore.mapper.UserSessionMapper;
import dev.xing.infinitechat.offlinedatastore.model.entity.UserSession;
import dev.xing.infinitechat.offlinedatastore.service.UserSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Slf4j
public class UserSessionServiceImpl extends ServiceImpl<UserSessionMapper, UserSession>
    implements UserSessionService {



    @Override
    public Set<Long> findSessionIdByUserId(Long userId) {
        QueryWrapper<UserSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<UserSession> userSessions = this.baseMapper.selectList(queryWrapper);
        log.info("userSessions:{}", userSessions);
        Set<Long> collect = userSessions.stream().map(UserSession::getSessionId).collect(Collectors.toSet());
        return collect;
    }
}








































