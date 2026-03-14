package com.easylive.entity.vo;

import com.easylive.entity.po.VideoInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VideoInfoResultVo {
    private VideoInfo videoInfo;

    public VideoInfoResultVo(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
    }

    public VideoInfoResultVo() {
    }

}
