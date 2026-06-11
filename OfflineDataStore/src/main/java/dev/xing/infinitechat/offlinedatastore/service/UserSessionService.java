package dev.xing.infinitechat.offlinedatastore.service;


import com.baomidou.mybatisplus.extension.service.IService;
import dev.xing.infinitechat.offlinedatastore.model.entity.UserSession;

import java.util.Set;

/**
* @description 针对表【user_session】的数据库操作Service
* @createDate 2024-09-20 16:41:50
*/
public interface UserSessionService extends IService<UserSession> {


     Set<Long> findSessionIdByUserId(Long userId);
}
