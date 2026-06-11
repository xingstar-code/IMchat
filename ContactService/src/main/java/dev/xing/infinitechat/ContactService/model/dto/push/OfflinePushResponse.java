package dev.xing.infinitechat.ContactService.model.dto.push;

import lombok.Data;
import java.util.List;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OfflinePushResponse {

    private List<NewSessionNotification> newSessionPushes;

    private List<NewGroupSessionNotification> newGroupPushes;

    private List<FriendApplicationNotification> friendRequests;
}
