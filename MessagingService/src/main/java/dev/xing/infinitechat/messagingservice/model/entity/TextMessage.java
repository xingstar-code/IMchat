package dev.xing.infinitechat.messagingservice.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TextMessage extends AppMessage {

    private TextMessageBody body;

    @Override
    public String toString() {
        return super.toString();
    }
}
