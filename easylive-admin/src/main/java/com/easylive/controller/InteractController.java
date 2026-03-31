package com.easylive.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easylive.entity.po.VideoComment;
import com.easylive.entity.po.VideoDanmu;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.service.VideoCommentService;
import com.easylive.service.VideoDanmuService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import static com.easylive.entity.vo.ResponseVO.getSuccessResponseVO;

@RestController
@RequestMapping("/interact")
@Validated
public class InteractController {
    @Resource
    private VideoCommentService videoCommentService;

    @Resource
    private VideoDanmuService videoDanmuService;


    @RequestMapping("/loadDanmu")
    public ResponseVO loadDanmu(Integer pageNo, String videoNameFuzzy) {
        pageNo= pageNo==null?1:pageNo;
        Page<VideoDanmu> page = videoDanmuService.selectJoinListPage(new Page<VideoDanmu>(pageNo, 15), VideoDanmu.class, new MPJLambdaWrapper<VideoDanmu>()
                .orderByDesc(VideoDanmu::getDanmuId)
                .leftJoin(VideoInfo.class, VideoInfo::getVideoId, VideoDanmu::getVideoId)
                .selectAll(VideoDanmu.class)
                .select(VideoInfo::getVideoCover, VideoInfo::getVideoName)
                .like(StringUtils.hasText(videoNameFuzzy), VideoInfo::getVideoName, videoNameFuzzy));
        return getSuccessResponseVO(new PaginationResultVO<>((int) page.getTotal(),15,pageNo,page.getRecords()));
    }


    @RequestMapping("/delDanmu")
    public ResponseVO delDanmu(@NotNull Integer danmuId) {
        videoDanmuService.deleteDanmu(null, danmuId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadComment")
    public ResponseVO loadComment(Integer pageNo, String videoNameFuzzy) {
        pageNo= pageNo==null?1:pageNo;
        Page<VideoComment> page = videoCommentService.selectJoinListPage(new Page<VideoComment>(pageNo, 15), VideoComment.class, new MPJLambdaWrapper<VideoComment>()
                .orderByDesc(VideoComment::getCommentId)
                .leftJoin(VideoInfo.class, VideoInfo::getVideoId, VideoComment::getVideoId)
                .selectAll(VideoComment.class)
                .select(VideoInfo::getVideoCover, VideoInfo::getVideoName)
                .like(StringUtils.hasText(videoNameFuzzy), VideoInfo::getVideoName, videoNameFuzzy));
        return getSuccessResponseVO(new PaginationResultVO<>((int) page.getTotal(),15,pageNo,page.getRecords()));
    }

    @RequestMapping("/delComment")
    public ResponseVO delComment(@NotNull Integer commentId) {
        videoCommentService.deleteComment(commentId, null);
        return getSuccessResponseVO(null);
    }
}
