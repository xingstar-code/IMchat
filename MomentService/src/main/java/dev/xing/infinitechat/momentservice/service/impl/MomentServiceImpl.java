package dev.xing.infinitechat.momentservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

import com.alibaba.nacos.shaded.com.google.gson.reflect.TypeToken;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xing.infinitechat.momentservice.config.MinioConfig;
import dev.xing.infinitechat.momentservice.mapper.MomentMapper;
import dev.xing.infinitechat.momentservice.model.entity.*;
import dev.xing.infinitechat.momentservice.model.enums.ConfigEnum;
import dev.xing.infinitechat.momentservice.model.enums.NoticeMomentEnum;
import dev.xing.infinitechat.momentservice.model.vo.*;
import dev.xing.infinitechat.momentservice.service.*;
import dev.xing.infinitechat.momentservice.utlis.ContentTypeUtil;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description 针对表【moment(朋友圈)】的数据库操作Service实现
 * @createDate 2024-10-08 11:59:32
 */
@Service
@Slf4j
@SuppressWarnings({"all"})
public class MomentServiceImpl extends ServiceImpl<MomentMapper, Moment> implements MomentService {

    @Autowired
    private MomentLikeService momentLikeService;

    @Autowired
    private MomentCommentService momentCommentService;

    @Autowired
    private UserService userService;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private FriendService friendService;

    private final static Gson GSON = new Gson();

    @Override
    public List<String> saveMomentMedias(MultipartFile[] files){
        List<String> urls = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue; // 忽略空文件
            } else {
                String bucketName = ConfigEnum.MINIO_BUCKET_NAME.getValue();
                try {
                    if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                    }
                    String fileSuffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
                    Snowflake snowflake = IdUtil.getSnowflake(Integer.parseInt(ConfigEnum.WORKED_ID.getValue()), Integer.parseInt(ConfigEnum.DATACENTER_ID.getValue()));
                    String fileName = snowflake.nextIdStr() + "." + fileSuffix;
                    // 获取上传文件的输入流
                    InputStream inputStream = file.getInputStream();
                    String contentType = new ContentTypeUtil().getType(fileSuffix);
                    minioClient.putObject(PutObjectArgs.builder().bucket(bucketName)      // 存储桶名称
                            .object(fileName)         // 对象名称，通常是文件名
                            .stream(inputStream, file.getSize(), -1) // 文件输入流和文件大小
                            .contentType(contentType)
                            .build());
                    String url = ConfigEnum.IMAGE_URI.getValue() + fileName;
                    urls.add(url);
                } catch (Exception e) {
                    log.error("上传图片失败，错误信息为：{}", e.getMessage());
                }
            }
            log.info("上传图片成功，图片地址为：{}", urls);
        }
        return urls;
    }

    @Override
    public MomentVO saveMoment(Long userId, String text, List<String> urls) {
        String mediaUrls = GSON.toJson(urls);
        Snowflake snowflake = IdUtil.getSnowflake(Integer.parseInt(ConfigEnum.WORKED_ID.getValue()), Integer.parseInt(ConfigEnum.DATACENTER_ID.getValue()));
        Moment moment = new Moment();
        moment.setUserId(userId);
        moment.setText(text);
        moment.setMediaUrl(mediaUrls);
        moment.setMomentId(snowflake.nextId());
        this.save(moment);
        log.info("保存朋友圈成功，朋友圈信息为：{}", moment);
        MomentVO momentVO = new MomentVO();
        BeanUtil.copyProperties(moment, momentVO);
        momentVO.setMediaUrls(urls);
        return momentVO;
    }

    @Override
    public boolean deleteMoment(Long momentId, Long userId) {
        QueryWrapper<Moment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("moment_id", momentId).eq("user_id", userId);
        Moment moment = this.getOne(queryWrapper);
        Date deleteTime = new Date();
        moment.setDeleteTime(deleteTime);
        moment.setUpdateTime(deleteTime);
        momentLikeService.remove(new QueryWrapper<MomentLike>().eq("moment_id", momentId));
        momentCommentService.remove(new QueryWrapper<MomentComment>().eq("moment_id", momentId));
        return this.update(moment, queryWrapper);
    }


    @Override
    public List<MomentOfflineVO> getOfflineNotice(Long userId, String time) {
        List<MomentOfflineVO> momentOfflineVOList = new ArrayList<>();
        // 获取好友 userId
        List<Long> friendIds = friendService.getFriendIds(userId);
        QueryWrapper<Moment> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", friendIds);
        queryWrapper.gt("create_time", time);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("LIMIT 1");
        if (friendIds == null || friendIds.size() == 0) {
            return momentOfflineVOList;
        }
        Moment moment = this.getOne(queryWrapper);
        // 获取朋友圈通知信息
        if (moment != null) {
            MomentOfflineVO momentOfflineVO = new MomentOfflineVO();
            momentOfflineVO.setNoticeType(NoticeMomentEnum.CREATE_MOMENT_NOTICE.getValue());
            User user = userService.getById(moment.getUserId());
            momentOfflineVO.setAvatar(user.getAvatar());
            momentOfflineVO.setTotal(1);
            momentOfflineVO.setType(NoticeMomentEnum.MOMENT_NOTICE.getValue());
            momentOfflineVOList.add(momentOfflineVO);
        }
        // 获取点赞评论通知信息
        QueryWrapper<Moment> queryWrapperMoment = new QueryWrapper<>();
        queryWrapperMoment.eq("user_id", userId);
        List<Moment> momentList = this.list(queryWrapperMoment);
        List<Long> momentIds = momentList.stream().map(Moment::getMomentId).collect(Collectors.toList());
        if (momentIds != null && momentIds.size() > 0) {
            QueryWrapper<MomentLike> queryWrapperLike = new QueryWrapper<>();
            queryWrapperLike.in("moment_id", momentIds);
            queryWrapperLike.gt("create_time", time);
            List<MomentLike> momentLike = momentLikeService.list(queryWrapperLike);
            QueryWrapper<MomentComment> queryWrapperComment = new QueryWrapper<>();
            queryWrapperComment.in("moment_id", momentIds);
            queryWrapperComment.gt("create_time", time);
            List<MomentComment> momentComment = momentCommentService.list(queryWrapperComment);
            if (momentLike != null || momentComment != null) {
                MomentOfflineVO momentOfflineVO = new MomentOfflineVO();
                momentOfflineVO.setNoticeType(NoticeMomentEnum.CREATE_MOMENT_COMMENT_LIKE_NOTICE.getValue());
                momentOfflineVO.setTotal(momentLike.size() + momentComment.size());
                momentOfflineVO.setType(NoticeMomentEnum.MOMENT_NOTICE.getValue());
                momentOfflineVOList.add(momentOfflineVO);
            }
        }
        return momentOfflineVOList;
    }

    @Override
    public Long getMomentIdByUserId(Long userId) {
        QueryWrapper<Moment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("LIMIT 1");
        Moment moment = this.getOne(queryWrapper);
        if (moment != null) {
            return moment.getMomentId();
        }
        return null;
    }


    @Override
    public MomentsListVO getMoments(List<Long> userIds, String time) {
        List<MomentsVO> momentsVOS = new ArrayList<>();
        // 根据用户 id 和 time 查询朋友圈
        QueryWrapper<Moment> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIds);
        queryWrapper.isNull("delete_time");
        queryWrapper.gt("create_time", time);
        queryWrapper.orderByDesc("create_time");
        List<Moment> moments = this.list(queryWrapper);
        for (Moment moment : moments) {
            MomentsVO momentsVO = new MomentsVO();
            momentsVO.setMomentId(moment.getMomentId());
            momentsVO.setUserId(moment.getUserId());
            momentsVO.setText(moment.getText());
            User user = userService.getById(moment.getUserId());
            momentsVO.setUserName(user.getUserName());
            momentsVO.setAvatar(user.getAvatar());
            List<String> mediaUrls = GSON.fromJson(moment.getMediaUrl(), new TypeToken<List<String>>() {
            }.getType());
            momentsVO.setMediaUrls(mediaUrls);
            Date date = moment.getCreateTime();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            momentsVO.setCreateTime(formatter.format(date));
            // 根据朋友圈 id 获取点赞列表
            QueryWrapper<MomentLike> queryWrapperLike = new QueryWrapper<>();
            queryWrapperLike.eq("moment_id", moment.getMomentId());
            queryWrapperLike.eq("is_delete", 0);
            List<MomentLike> MomentLikes = momentLikeService.list(queryWrapperLike);
            List<LikeVO> likeVOS = new ArrayList<>();
            for (MomentLike momentLike : MomentLikes) {
                User userLike = userService.getById(momentLike.getUserId());
                if (userLike != null) {
                    LikeVO likeVO = new LikeVO();
                    likeVO.setLikeId(momentLike.getLikeId());
                    likeVO.setUserName(userLike.getUserName());
                    likeVO.setUserId(userLike.getUserId());
                    likeVOS.add(likeVO);
                }
            }
            momentsVO.setLikes(likeVOS);
            // 根据朋友圈 id 获取评论列表
            QueryWrapper<MomentComment> queryWrapperComment = new QueryWrapper<>();
            queryWrapperComment.eq("moment_id", moment.getMomentId());
            queryWrapperComment.eq("is_delete", 0);
            List<MomentComment> MomentComments = momentCommentService.list(queryWrapperComment);
            List<MomentCommentVO> momentCommentVOS = new ArrayList<>();
            for (MomentComment momentComment : MomentComments) {
                MomentCommentVO momentCommentVO = new MomentCommentVO();
                User userComment = userService.getById(momentComment.getUserId());
                if (userComment != null) {
                    momentCommentVO.setCommentId(momentComment.getCommentId());
                    momentCommentVO.setUserName(userComment.getUserName());
                    momentCommentVO.setComment(momentComment.getComment());
                    if (momentComment.getParentCommentId() != null) {
                        MomentComment parentComment = momentCommentService.getById(momentComment.getParentCommentId());
                        if (parentComment != null) {
                            User parentUser = userService.getById(parentComment.getUserId());
                            momentCommentVO.setParentUserName(parentUser.getUserName());
                            momentCommentVO.setParentCommentId(parentComment.getCommentId());
                            momentCommentVOS.add(momentCommentVO);
                        }
                    } else {
                        momentCommentVOS.add(momentCommentVO);
                    }
                }
            }
            momentsVO.setComments(momentCommentVOS);
            momentsVOS.add(momentsVO);
        }
        log.info("查询朋友圈成功，朋友圈信息为：{}", moments);
        // 获取删除的朋友圈
        QueryWrapper<Moment> queryWrapperDelete = new QueryWrapper<>();
        queryWrapperDelete.in("user_id", userIds);
        queryWrapperDelete.ge("delete_time", time);
        queryWrapperDelete.isNotNull("delete_time");
        List<Moment> deleteMoments = this.list(queryWrapperDelete);
        List<CreateLikeVO> createLikeListVOS = new ArrayList<>();
        List<DeleteLikeVO> deleteLikeListVOS = new ArrayList<>();
        List<CreateCommentVO> createCommentListVOS = new ArrayList<>();
        List<DeleteCommentVO> deleteCommentListVOS = new ArrayList<>();
        if (userIds.size() == 1) {
            List<Long> friendIds = friendService.getFriendIds(userIds.get(0));
            userIds.addAll(friendIds);
        }
        QueryWrapper<MomentLike> queryWrapperLike = new QueryWrapper<>();
        queryWrapperLike.in("user_id", userIds);
        queryWrapperLike.gt("update_time", time);
        List<MomentLike> Likes = momentLikeService.list(queryWrapperLike);
        log.info(Likes.toString());
        for (MomentLike like : Likes) {
            if (like.getIsDelete() == 1) {
                DeleteLikeVO deleteLikeVO = new DeleteLikeVO();
                BeanUtil.copyProperties(like, deleteLikeVO);
                deleteLikeListVOS.add(deleteLikeVO);
            } else {
                CreateLikeVO createLikeVO = new CreateLikeVO();
                BeanUtil.copyProperties(like, createLikeVO);
                User user = userService.getById(like.getUserId());
                createLikeVO.setUserName(user.getUserName());
                createLikeListVOS.add(createLikeVO);
            }
        }
        QueryWrapper<MomentComment> queryWrapperComment = new QueryWrapper<>();
        queryWrapperComment.in("user_id", userIds);
        queryWrapperComment.gt("update_time", time);
        List<MomentComment> Comments = momentCommentService.list(queryWrapperComment);
        log.info(Comments.toString());
        for (MomentComment comment : Comments) {
            if (comment.getIsDelete() == 1) {
                DeleteCommentVO deleteCommentVO = new DeleteCommentVO();
                BeanUtil.copyProperties(comment, deleteCommentVO);
                deleteCommentListVOS.add(deleteCommentVO);
            } else {
                CreateCommentVO createCommentVO = new CreateCommentVO();
                BeanUtil.copyProperties(comment, createCommentVO);
                User user = userService.getById(comment.getUserId());
                createCommentVO.setUserName(user.getUserName());
                createCommentVO.setComment(comment.getComment());
                if (comment.getParentCommentId() != null) {
                    MomentComment parentComment = momentCommentService.getById(comment.getParentCommentId());
                    User parentUser = userService.getById(parentComment.getUserId());
                    createCommentVO.setParentUserName(parentUser.getUserName());
                    createCommentVO.setParentCommentId(parentComment.getCommentId());
                }
                createCommentListVOS.add(createCommentVO);
            }
        }
        MomentsListVO momentsListVO = new MomentsListVO();
        momentsListVO.setMomentsList(momentsVOS);
        momentsListVO.setDeleteMomentsIds(deleteMoments.stream().map(Moment::getMomentId).collect(Collectors.toList()));
        momentsListVO.setCreateLikeList(createLikeListVOS);
        momentsListVO.setDeleteLikeList(deleteLikeListVOS);
        momentsListVO.setCreateCommentList(createCommentListVOS);
        momentsListVO.setDeleteCommentList(deleteCommentListVOS);
        return momentsListVO;
    }
}




