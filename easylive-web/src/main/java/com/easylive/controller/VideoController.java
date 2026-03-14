package com.easylive.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.po.VideoInfoFile;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.entity.vo.UserLoginDto;
import com.easylive.entity.vo.VideoInfoResultVo;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.enums.VideoRecommendTypeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.redis.RedisComponent;
import com.easylive.redis.RedisUtils;
import com.easylive.service.VideoInfoFileService;
import com.easylive.service.VideoInfoService;
import com.easylive.utils.CookieUtil;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import java.util.List;

import static com.easylive.entity.vo.ResponseVO.getSuccessResponseVO;

@RestController
@Validated
@RequestMapping("/video")
@Slf4j
public class VideoController {

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private VideoInfoFileService videoInfoFileService;


    @Resource
    private RedisComponent redisComponent;

    @Autowired
    private RedisUtils redisUtils;


    @RequestMapping("/loadRecommendVideo")
//    @GlobalInterceptor
    public ResponseVO loadRecommendVideo() {
        List<VideoInfo> recommendVideoList = videoInfoService.list(new MPJLambdaWrapper<VideoInfo>()
                .selectAll(VideoInfo.class) // 1. 查出 VideoInfo 表所有字段
                .select(UserInfo::getNickName, UserInfo::getAvatar) // 2. 查出 UserInfo 里的字段
                .eq(VideoInfo::getRecommendType, VideoRecommendTypeEnum.RECOMMEND.getType())
                .orderByDesc(VideoInfo::getCreateTime)
                .leftJoin(UserInfo.class,UserInfo::getUserId, VideoInfo::getUserId));
        return getSuccessResponseVO(recommendVideoList);
    }

    @RequestMapping("/loadVideo")
//    @GlobalInterceptor
    public ResponseVO loadVideo(Integer pCategoryId, Integer categoryId, Integer pageNo) {
        MPJLambdaWrapper<VideoInfo> wrapper = new MPJLambdaWrapper<VideoInfo>()
                .selectAll(VideoInfo.class)
                .select(UserInfo::getNickName, UserInfo::getAvatar)
                .leftJoin(UserInfo.class, UserInfo::getUserId, VideoInfo::getUserId)
                .orderByDesc(VideoInfo::getCreateTime)
                .eq(categoryId != null,VideoInfo::getCategoryId, categoryId)
                .eq(pCategoryId != null,VideoInfo::getPCategoryId, pCategoryId);
        if (categoryId == null && pCategoryId == null){
            wrapper.eq(VideoInfo::getRecommendType,VideoRecommendTypeEnum.NO_RECOMMEND.getType());
        }
        IPage<VideoInfo> iPage = videoInfoService.selectJoinListPage(new Page<>(pageNo == null ? 1 : pageNo, 15),
                VideoInfo.class,wrapper);
        PaginationResultVO resultVO = new PaginationResultVO<>((int)iPage.getTotal(),(int)iPage.getSize(),(int)iPage.getCurrent(),iPage.getRecords());
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/getVideoInfo")
//    @GlobalInterceptor
    public ResponseVO getVideoInfo(@NotEmpty String videoId, HttpServletRequest request) {
        VideoInfo videoInfo = videoInfoService.getById(videoId);
        if (null == videoInfo) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        String token = CookieUtil.getCookieToken(request);
        UserLoginDto tokenUserInfoDto = (UserLoginDto) redisUtils.get(Constants.REDIS_KEY_LOGIN_TOKEN + token);
//todo 获取用户行为

//        List<UserAction> userActionList = new ArrayList<>();
//        if (userInfoDto != null) {
//            UserActionQuery actionQuery = new UserActionQuery();
//            actionQuery.setVideoId(videoId);
//            actionQuery.setUserId(userInfoDto.getUserId());
//            actionQuery.setActionTypeArray(new Integer[]{UserActionTypeEnum.VIDEO_LIKE.getType(), UserActionTypeEnum.VIDEO_COLLECT.getType(),
//                    UserActionTypeEnum.VIDEO_COIN.getType(),});
//            userActionList = userActionService.findListByParam(actionQuery);
//        }
        VideoInfoResultVo resultVo = new VideoInfoResultVo();
        resultVo.setVideoInfo(videoInfo);
//        resultVo.setUserActionList(userActionList);
        return getSuccessResponseVO(resultVo);
    }

    @RequestMapping("/loadVideoPList")
//    @GlobalInterceptor
    public ResponseVO loadVideoPList(@NotEmpty String videoId) {
//        VideoInfoFileQuery videoInfoQuery = new VideoInfoFileQuery();
//        videoInfoQuery.setVideoId(videoId);
//        videoInfoQuery.setOrderBy("file_index asc");
//        List<VideoInfoFile> fileList = videoInfoFileService.findListByParam(videoInfoQuery);
        List<VideoInfoFile> fileList = videoInfoFileService.list(new LambdaQueryWrapper<VideoInfoFile>()
                .eq(VideoInfoFile::getVideoId, videoId)
                .orderByAsc(VideoInfoFile::getFileIndex));
        return getSuccessResponseVO(fileList);
    }
}