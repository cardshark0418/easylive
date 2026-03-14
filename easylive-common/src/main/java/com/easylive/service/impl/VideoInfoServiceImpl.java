package com.easylive.service.impl;

import com.easylive.entity.po.VideoInfo;
import com.easylive.mapper.VideoInfoMapper;
import com.easylive.service.VideoInfoService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class VideoInfoServiceImpl extends MPJBaseServiceImpl<VideoInfoMapper, VideoInfo> implements VideoInfoService {
}
