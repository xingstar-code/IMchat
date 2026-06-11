package dev.xing.infinitechat.messagingservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import dev.xing.infinitechat.messagingservice.model.dto.SendMsgRequest;
import dev.xing.infinitechat.messagingservice.model.entity.Message;
import dev.xing.infinitechat.messagingservice.model.vo.ResponseMsgVo;
import org.springframework.stereotype.Service;

@Service
public interface MessagingService extends IService<Message> {

    ResponseMsgVo sendMessage(SendMsgRequest sendMsgRequest) throws Exception;
}