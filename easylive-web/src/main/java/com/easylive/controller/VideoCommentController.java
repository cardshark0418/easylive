package com.easylive.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.easylive.entity.po.UserAction;
import com.easylive.entity.po.VideoComment;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.entity.vo.UserLoginDto;
import com.easylive.entity.vo.VideoCommentResultVO;
import com.easylive.enums.UserActionTypeEnum;
import com.easylive.redis.RedisComponent;
import com.easylive.service.UserActionService;
import com.easylive.service.VideoCommentService;
import com.easylive.service.impl.VideoInfoServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.easylive.entity.vo.ResponseVO.getSuccessResponseVO;

@RestController
@Validated
@RequestMapping("/comment")
@Slf4j
public class VideoCommentController{

    @Resource
    private VideoCommentService videoCommentService;

    @Resource
    private UserActionService userActionService;

    @Resource
    private VideoInfoServiceImpl videoInfoService;

    @Autowired
    private RedisComponent redisComponent;

    @RequestMapping("/postComment")
//    @GlobalInterceptor(checkLogin = true)
//    @RecordUserMessage(messageType = MessageTypeEnum.COMMENT)
    public ResponseVO postComment(@NotEmpty String videoId,
                                  Integer replyCommentId,
                                  @NotEmpty @Size(max = 500) String content,
                                  @Size(max = 50) String imgPath,
                                  HttpServletRequest request) {

        UserLoginDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        VideoComment comment = new VideoComment();
        comment.setUserId(tokenUserInfoDto.getUserId());
        comment.setAvatar(tokenUserInfoDto.getAvatar());
        comment.setNickName(tokenUserInfoDto.getNickName());
        comment.setVideoId(videoId);
        comment.setContent(content);
        comment.setImgPath(imgPath);
        videoCommentService.postComment(comment, replyCommentId);
        return getSuccessResponseVO(comment);
    }

    @RequestMapping("/loadComment")
//    @GlobalInterceptor
    public ResponseVO loadComment(
            @NotEmpty String videoId,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "0") Integer orderType,
            HttpServletRequest request) {
        // 1. 互动权限校验
        VideoInfo videoInfo = videoInfoService.getById(videoId);
        if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains("1")) {
            return getSuccessResponseVO(new VideoCommentResultVO());
        }

        // 2. 直接获取分页数据（已包含二级评论）
        PaginationResultVO<VideoComment> commentData = videoCommentService.getCommentList(videoId, pageNo, orderType);

        // 3. 处理置顶逻辑 (如果是第一页)
        // 1. 获取当前页的父评论列表（假设你的 commentData.getList() 已经查出了当前页的父评论）
        List<VideoComment> rootList = commentData.getList();

// 2. 处理置顶评论逻辑：确保第一页包含置顶评论且不重复
        if (pageNo == null || pageNo == 1) {
            VideoComment topComment = videoCommentService.getOne(new LambdaQueryWrapper<VideoComment>()
                    .eq(VideoComment::getVideoId, videoId)
                    .eq(VideoComment::getTopType, 1)
                    .last("LIMIT 1")); // 明确只取一条

            if (topComment != null) {
                // 过滤掉列表里可能已经存在的同 ID 评论，并将置顶评论插入到头部
                rootList = rootList.stream()
                        .filter(item -> !item.getCommentId().equals(topComment.getCommentId()))
                        .collect(Collectors.toList());
                rootList.add(0, topComment); // 插入到第一条
            }
        }

// 3. 核心：查出这些父评论对应的所有子评论
        if (!rootList.isEmpty()) {
            // 提取当前页所有父评论的 ID
            List<Integer> parentIds = rootList.stream()
                    .map(VideoComment::getCommentId)
                    .collect(Collectors.toList());

            // 一次性查询所有子评论 (p_comment_id 在上述 ID 集合中)
            List<VideoComment> allChildren = videoCommentService.list(new LambdaQueryWrapper<VideoComment>()
                    .in(VideoComment::getPCommentId, parentIds)
                    .orderByAsc(VideoComment::getPostTime)); // 子评论通常按时间正序

            // 4. 将子评论按 p_comment_id 分组映射
            Map<Integer, List<VideoComment>> childrenMap = allChildren.stream()
                    .collect(Collectors.groupingBy(VideoComment::getPCommentId));

            // 5. 将分组后的子评论塞进父评论的 children 属性中
            rootList.forEach(parent -> {
                parent.setChildren(childrenMap.getOrDefault(parent.getCommentId(), new ArrayList<>()));
            });
        }

        // 6. 写回结果
        commentData.setList(rootList);

        // 4. 获取用户点赞/踩的状态 (UserAction 部分建议保持独立查询，性能更好且逻辑解耦)
        VideoCommentResultVO resultVO = new VideoCommentResultVO();
        resultVO.setCommentData(commentData);
        UserLoginDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        List<UserAction> userActionList = userActionService.list(new LambdaQueryWrapper<UserAction>()
                .eq(UserAction::getUserId, tokenUserInfoDto.getUserId())
                .eq(UserAction::getVideoId, videoId)
                .in(UserAction::getActionType, (Object[]) new Integer[]{UserActionTypeEnum.COMMENT_LIKE.getType(), UserActionTypeEnum.COMMENT_HATE.getType()}));
        resultVO.setUserActionList(userActionList);
        return getSuccessResponseVO(resultVO);
    }

}