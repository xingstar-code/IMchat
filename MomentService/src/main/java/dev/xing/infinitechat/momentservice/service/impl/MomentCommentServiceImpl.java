package dev.xing.infinitechat.momentservice.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xing.infinitechat.momentservice.mapper.MomentCommentMapper;
import dev.xing.infinitechat.momentservice.model.dto.MomentCommentDTO;
import dev.xing.infinitechat.momentservice.model.entity.MomentComment;
import dev.xing.infinitechat.momentservice.model.entity.User;
import dev.xing.infinitechat.momentservice.model.enums.ConfigEnum;
import dev.xing.infinitechat.momentservice.model.vo.MomentCommentVO;
import dev.xing.infinitechat.momentservice.service.MomentCommentService;
import dev.xing.infinitechat.momentservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
* @description 针对表【moment_comment(朋友圈评论)】的数据库操作Service实现
* @createDate 2024-10-08 16:37:48
*/
@Service
@Slf4j
@SuppressWarnings({"all"})
public class MomentCommentServiceImpl extends ServiceImpl<MomentCommentMapper, MomentComment>
    implements MomentCommentService {

    @Autowired
    private UserService userService;

    @Override
    public MomentCommentVO createComment(Long momentId, MomentCommentDTO momentCommentDTO) {
        MomentComment momentComment = new MomentComment();
        Snowflake snowflake = IdUtil.getSnowflake(Integer.parseInt(ConfigEnum.WORKED_ID.getValue()), Integer.parseInt(ConfigEnum.DATACENTER_ID.getValue()));
        momentComment.setCommentId(snowflake.nextId());
        momentComment.setComment(momentCommentDTO.getComment());
        momentComment.setMomentId(momentId);
        momentComment.setUserId(momentCommentDTO.getUserId());
        momentComment.setIsDelete(0);
        if (momentCommentDTO.getParentCommentId() != null) {
            momentComment.setParentCommentId(momentCommentDTO.getParentCommentId());
        }
        this.save(momentComment);
        MomentCommentVO momentCommentVO = new MomentCommentVO();
        BeanUtil.copyProperties(momentComment, momentCommentVO);
        User user = userService.getById(momentCommentDTO.getUserId());
        momentCommentVO.setUserName(user.getUserName());
        if (momentCommentDTO.getParentCommentId() != null) {
            MomentComment parentComment = this.getById(momentCommentDTO.getParentCommentId());
            Long parentUserId = parentComment.getUserId();
            User parentUser = userService.getById(parentUserId);
            momentCommentVO.setParentUserName(parentUser.getUserName());
        }

        return momentCommentVO;
    }

    @Override
    public boolean deleteComment(Long momentId, Long commentId, Long userId) {
        return deleteCommentRecursively(momentId, commentId, userId);
    }

    private boolean deleteCommentRecursively(Long momentId, Long commentId, Long userId) {
        // 查询当前评论的所有子评论
        QueryWrapper<MomentComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("moment_id", momentId);
        queryWrapper.eq("parent_comment_id", commentId); // 假设有 parent_comment_id 字段
        List<MomentComment> childComments = this.list(queryWrapper);
        // 递归删除所有子评论
        for (MomentComment childComment : childComments) {
            deleteCommentRecursively(momentId, childComment.getCommentId(), childComment.getUserId());
        }
        // 逻辑删除当前评论
        QueryWrapper<MomentComment> queryWrapperParent = new QueryWrapper<>();
        queryWrapperParent.eq("moment_id", momentId);
        queryWrapperParent.eq("comment_id", commentId);
        queryWrapperParent.eq("user_id", userId);
        MomentComment momentComment = this.getOne(queryWrapperParent);
        if (momentComment != null) {
            momentComment.setIsDelete(1);
            momentComment.setUpdateTime(new Date());
            log.info(momentComment.toString());
            return this.update(momentComment, queryWrapperParent);
        }
        return false;
    }
}




