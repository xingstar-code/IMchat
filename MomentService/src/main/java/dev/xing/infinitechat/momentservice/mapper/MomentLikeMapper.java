package dev.xing.infinitechat.momentservice.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.xing.infinitechat.momentservice.model.entity.MomentLike;
import org.apache.ibatis.annotations.Mapper;

/**
* @description 针对表【moment_like(朋友圈点赞)】的数据库操作Mapper
* @createDate 2024-10-14 15:23:31
* @Entity generator.domain.MomentLike
*/
@Mapper
public interface MomentLikeMapper extends BaseMapper<MomentLike> {

}




