package dev.xing.infinitechat.ContactService.service;

import com.baomidou.mybatisplus.extension.service.IService;
import dev.xing.infinitechat.ContactService.model.entity.Session;
import dev.xing.infinitechat.ContactService.model.dto.CreateGroupRequest;
import dev.xing.infinitechat.ContactService.model.dto.CreateGroupResponse;

public interface SessionService extends IService<Session> {
    CreateGroupResponse createGroup(CreateGroupRequest request);
}