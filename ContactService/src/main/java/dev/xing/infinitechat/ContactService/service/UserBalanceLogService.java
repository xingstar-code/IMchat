package dev.xing.infinitechat.ContactService.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xing.infinitechat.common.ServiceException;
import dev.xing.infinitechat.ContactService.mapper.BalanceLogMapper;
import dev.xing.infinitechat.ContactService.model.dto.BalanceLogDTO;
import dev.xing.infinitechat.ContactService.model.dto.UserBalanceLogResponse;
import dev.xing.infinitechat.ContactService.model.entity.BalanceLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserBalanceLogService extends ServiceImpl<BalanceLogMapper, BalanceLog> {

    @Autowired
    private BalanceLogMapper balanceLogMapper;

    public UserBalanceLogResponse getBalanceDetail(Long userId, Integer pageNum, Integer pageSize) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        List<BalanceLogDTO> balanceLogs = balanceLogMapper.selectBalanceLogsByUserId(userId, (pageNum - 1) * pageSize, pageSize);

        QueryWrapper<BalanceLog> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return new UserBalanceLogResponse(balanceLogs, Math.toIntExact(balanceLogMapper.selectCount(wrapper)));
    }
}