package dev.xing.infinitechat.momentservice.service;


import com.baomidou.mybatisplus.extension.service.IService;
import dev.xing.infinitechat.momentservice.model.entity.Friend;

import java.util.List;

/**
* @description 针对表【friend(联系人表)】的数据库操作Service
* @createDate 2024-10-08 15:09:48
*/
public interface FriendService extends IService<Friend> {



    public List<Long> getFriendIds(Long userId);
}
