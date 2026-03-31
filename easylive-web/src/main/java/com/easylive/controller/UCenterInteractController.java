package com.easylive.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easylive.annotation.GlobalInterceptor;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.po.VideoComment;
import com.easylive.entity.po.VideoDanmu;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.entity.vo.UserLoginDto;
import com.easylive.redis.RedisComponent;
import com.easylive.service.VideoCommentService;
import com.easylive.service.VideoDanmuService;
import com.easylive.service.VideoInfoService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;

import static com.easylive.entity.vo.ResponseVO.getSuccessResponseVO;

@RestController
@Validated
@RequestMapping("/ucenter")
@GlobalInterceptor(checkLogin = true)
public class UCenterInteractController{

    @Resource
    private VideoCommentService videoCommentService;

    @Resource
    private VideoDanmuService videoDanmuService;

    @Resource
    private VideoInfoService videoInfoService;

    @Autowired
    private RedisComponent redisComponent;

    @RequestMapping("/loadAllVideo")
//    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadAllVideo(HttpServletRequest request) {
        UserLoginDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        List<VideoInfo> videoInfoList = videoInfoService.list(new LambdaQueryWrapper<VideoInfo>()
                .eq(VideoInfo::getUserId,tokenUserInfoDto.getUserId())
                .orderByDesc(VideoInfo::getCreateTime));
        return getSuccessResponseVO(videoInfoList);
    }

    @RequestMapping("/loadComment")
//    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadComment(Integer pageNo, String videoId,HttpServletRequest request) {
        pageNo= pageNo==null?1:pageNo;
        UserLoginDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        MPJLambdaWrapper<VideoComment> wrapper = new MPJLambdaWrapper<VideoComment>()
                .selectAll(VideoComment.class)
                .selectAs(VideoInfo::getVideoName, VideoComment::getVideoName)
                .selectAs(VideoInfo::getVideoCover, VideoComment::getVideoCover)
                .eq(VideoInfo::getUserId, tokenUserInfoDto.getUserId())
                .orderByDesc("comment_id")
                .leftJoin(VideoInfo.class, VideoInfo::getVideoId, VideoComment::getVideoId);
        if(videoId!=null && !videoId.isEmpty()){
            wrapper.eq(VideoComment::getVideoId, videoId);
        }
        Page<VideoComment> videoCommentPage = videoCommentService.selectJoinListPage(new Page<>(pageNo, 15),
                VideoComment.class, wrapper);
        return getSuccessResponseVO(new PaginationResultVO<>((int) videoCommentPage.getTotal(),15,pageNo,videoCommentPage.getRecords()));
    }

    @RequestMapping("/delComment")
//    @GlobalInterceptor(checkLogin = true)
    public ResponseVO delComment(@NotNull Integer commentId,HttpServletRequest request) {
        UserLoginDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        videoCommentService.deleteComment(commentId, tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadDanmu")
//    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadDanmu(Integer pageNo, String videoId,HttpServletRequest request) {
        pageNo= pageNo==null?1:pageNo;
        UserLoginDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        MPJLambdaWrapper<VideoDanmu> wrapper = new MPJLambdaWrapper<VideoDanmu>()
                .selectAll(VideoDanmu.class)
                .selectAs(VideoInfo::getVideoName, VideoDanmu::getVideoName)
                .selectAs(VideoInfo::getVideoCover, VideoDanmu::getVideoCover)
                .selectAs(UserInfo::getNickName, VideoDanmu::getNickName)
                .eq(VideoInfo::getUserId, tokenUserInfoDto.getUserId())
                .orderByDesc("danmu_id")
                .leftJoin(VideoInfo.class, VideoInfo::getVideoId, VideoDanmu::getVideoId)
                .leftJoin(UserInfo.class, UserInfo::getUserId, VideoDanmu::getUserId);
        if(videoId!=null && !videoId.isEmpty()){
            wrapper.eq(VideoDanmu::getVideoId, videoId);
        }
        Page<VideoDanmu> videoDanmuPage = videoDanmuService.selectJoinListPage(new Page<>(pageNo, 15),
                VideoDanmu.class, wrapper);
        return getSuccessResponseVO(new PaginationResultVO<>((int) videoDanmuPage.getTotal(),15,pageNo,videoDanmuPage.getRecords()));
    }

    @RequestMapping("/delDanmu")
//    @GlobalInterceptor(checkLogin = true)
    public ResponseVO delDanmu(@NotNull Integer danmuId,HttpServletRequest request) {
        UserLoginDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(request);
        videoDanmuService.deleteDanmu(tokenUserInfoDto.getUserId(), danmuId);
        return getSuccessResponseVO(null);
    }
}
