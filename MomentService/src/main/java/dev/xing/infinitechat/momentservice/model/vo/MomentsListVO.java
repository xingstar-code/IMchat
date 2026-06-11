package dev.xing.infinitechat.momentservice.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MomentsListVO implements Serializable {

    private List<MomentsVO> momentsList;

    private List<CreateLikeVO> createLikeList;

    private List<DeleteLikeVO> deleteLikeList;

    private List<CreateCommentVO> createCommentList;

    private List<DeleteCommentVO> deleteCommentList;

    private List<Long> deleteMomentsIds;
}
