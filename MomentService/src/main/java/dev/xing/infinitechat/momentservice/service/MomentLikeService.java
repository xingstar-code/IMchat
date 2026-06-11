package dev.xing.infinitechat.momentservice.service;


import com.baomidou.mybatisplus.extension.service.IService;
import dev.xing.infinitechat.momentservice.model.entity.MomentLike;

/**
* @description 针对表【moment_like(朋友圈点赞)】的数据库操作Service
* @createDate 2024-10-08 15:50:26
*/
@SuppressWarnings({"all"})
public interface MomentLikeService extends IService<MomentLike> {



    public Long createLike(Long momentId, Long userId);

    public boolean deleteLike(Long momentId, Long LikeId, Long userId);
}
