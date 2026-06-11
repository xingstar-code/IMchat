package dev.xing.infinitechat.messagingservice.utils;

import dev.xing.infinitechat.messagingservice.service.RedPacketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RedPacketExpirationListener implements MessageListener {

    @Autowired
    private RedPacketService redPacketService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // Get the expired key
        String expiredKey = message.toString();
        log.info("得到过期的键: " + expiredKey);
        if (expiredKey.startsWith("red_packet:count:")) {
            String redPacketIdStr = expiredKey.substring("red_packet:count:".length());
            Long redPacketId = Long.parseLong(redPacketIdStr);

            // Handle red packet expiration
            log.info("得到过期的红包Id: " + redPacketId);
            try {
                redPacketService.handleExpiredRedPacket(redPacketId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
