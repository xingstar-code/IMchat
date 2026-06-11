package dev.xing.infinitechat.messagingservice.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PictureMessage extends AppMessage {

    private PictureMessageBody body;

    @Override
    public String toString() {
        return super.toString();
    }
}
