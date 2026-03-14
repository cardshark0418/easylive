package com.easylive.service.impl;

import com.easylive.entity.po.VideoComment;
import com.easylive.mapper.VideoCommentMapper;
import com.easylive.service.VideoCommentService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class VideoCommentServiceImpl extends MPJBaseServiceImpl<VideoCommentMapper, VideoComment> implements VideoCommentService {
}