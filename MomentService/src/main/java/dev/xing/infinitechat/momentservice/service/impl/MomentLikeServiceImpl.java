package dev.xing.infinitechat.momentservice.service.impl;


import java.util.Date;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xing.infinitechat.momentservice.mapper.MomentLikeMapper;

import dev.xing.infinitechat.momentservice.model.entity.MomentLike;
import dev.xing.infinitechat.momentservice.model.enums.ConfigEnum;
import dev.xing.infinitechat.momentservice.service.MomentLikeService;
import dev.xing.infinitechat.momentservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description 针对表【moment_like(朋友圈点赞)】的数据库操作Service实现
 * @createDate 2024-10-08 15:50:26
 */
@Service
@SuppressWarnings({"all"})
public class MomentLikeServiceImpl extends ServiceImpl<MomentLikeMapper, MomentLike> implements MomentLikeService {


    @Autowired
    private UserService userService;

    @Override
    public Long createLike(Long momentId, Long userId) {
        MomentLike like = new MomentLike();
        Snowflake snowflake = IdUtil.getSnowflake(Integer.parseInt(ConfigEnum.WORKED_ID.getValue()), Integer.parseInt(ConfigEnum.DATACENTER_ID.getValue()));
        like.setLikeId(snowflake.nextId());
        like.setMomentId(momentId);
        like.setUserId(userId);
        like.setIsDelete(0);
        this.save(like);
        return like.getLikeId();
    }

    @Override
    public boolean deleteLike(Long momentId, Long likeId, Long userId) {
        QueryWrapper<MomentLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("moment_id", momentId);
        queryWrapper.eq("like_id", likeId);
        queryWrapper.eq("user_id", userId);
        MomentLike like = this.getOne(queryWrapper);
        like.setIsDelete(1);
        like.setUpdateTime(new Date());
        return this.update(like, queryWrapper);
    }
}




