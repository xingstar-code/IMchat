package dev.xing.infinitechat.momentservice.service;


import com.baomidou.mybatisplus.extension.service.IService;
import dev.xing.infinitechat.momentservice.model.dto.MomentCommentDTO;
import dev.xing.infinitechat.momentservice.model.entity.MomentComment;
import dev.xing.infinitechat.momentservice.model.vo.MomentCommentVO;

/**
* @description 针对表【moment_comment(朋友圈评论)】的数据库操作Service
* @createDate 2024-10-08 16:37:48
*/
@SuppressWarnings({"all"})
public interface MomentCommentService extends IService<MomentComment> {


    MomentCommentVO createComment(Long momentId, MomentCommentDTO momentCommentDTO);

    boolean deleteComment(Long momentId, Long commentId, Long userId);

}
