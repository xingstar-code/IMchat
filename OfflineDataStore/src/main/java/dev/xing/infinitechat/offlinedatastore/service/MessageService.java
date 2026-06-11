package dev.xing.infinitechat.offlinedatastore.service;


import com.baomidou.mybatisplus.extension.service.IService;
import dev.xing.infinitechat.offlinedatastore.model.entity.Message;
import dev.xing.infinitechat.offlinedatastore.model.vo.OfflineMsgDetail;

import java.util.List;

/**
* @description 针对表【message】的数据库操作Service
* @createDate 2024-09-20 16:39:30
*/
public interface MessageService extends IService<Message> {

    List<OfflineMsgDetail>  findOfflineMsgBySessionId(Long sessionId, String time);

}
