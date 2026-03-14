package com.easylive.redis;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.easylive.config.AppConfig;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.po.VideoInfoFilePost;
import com.easylive.entity.vo.SysSettingDto;
import com.easylive.entity.vo.UploadingFileDto;
import com.easylive.entity.vo.VideoPlayInfoDto;
import com.easylive.enums.DateTimePatternEnum;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;

    @Resource
    private AppConfig appConfig;

    public String savePreVideoFileInfo(String userId,String fileName,Integer chunks){
        String uploadId = RandomUtil.randomString(10);
        UploadingFileDto fileDto = new UploadingFileDto();
        fileDto.setChunks(chunks);
        fileDto.setFileName(fileName);
        fileDto.setUploadId(uploadId);
        fileDto.setChunkIndex(0);

        String day = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMMDD.getPattern());
        String filePath = day + "/" + userId + uploadId;

        String folder = appConfig.getProjectFolder() + "file/" + "temp/" + filePath;
        File folderFile = new File(folder);
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }
        fileDto.setFilePath(filePath);
        redisUtils.setex(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadId, fileDto, Constants.ONE_MIN_MILLS*60*24);
        return uploadId;
    }

    public UploadingFileDto getUploadingVideoFile(String userId, String uploadId) {
        return (UploadingFileDto) redisUtils.get(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadId);
    }

    public SysSettingDto getSysSettingDto() {
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if (sysSettingDto == null) {
            sysSettingDto = new SysSettingDto();
        }
        return sysSettingDto;
    }

    public void updateVideoFileInfo(String userId, UploadingFileDto uploadingFileDto) {
        redisUtils.setex(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadingFileDto.getUploadId(), uploadingFileDto, Constants.ONE_MIN_MILLS*60*24);
    }

    public void delVideoFileInfo(String userId, String uploadId) {
        redisUtils.delete(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadId);
    }

    public void addFile2DelQueue(String videoId, List<String> delFilePathList) {
        redisUtils.lpushAll(Constants.REDIS_KEY_FILE_DEL+videoId, Collections.singletonList(delFilePathList),Constants.ONE_MIN_MILLS*60*24);
    }

    public void addFile2TransferQueue(List<VideoInfoFilePost> fileList) {
        if (CollectionUtils.isEmpty(fileList)) {
            return;
        }
        // 循环放入，这样队列里存储的就是一个个独立的对象
        for (VideoInfoFilePost file : fileList) {
            redisUtils.lpush(Constants.REDIS_KEY_QUEUE_TRANSFER, file,(long)-1);
        }
    }

    public List<String> getDelFileList(String videoId) {
        List<String> filePathList = redisUtils.getQueueList(Constants.REDIS_KEY_FILE_DEL + videoId);
        return filePathList;
    }

    public void cleanDelFileList(String videoId) {
        redisUtils.delete(Constants.REDIS_KEY_FILE_DEL + videoId);
    }

    public void addVideoPlay(VideoPlayInfoDto videoPlayInfoDto) {
        redisUtils.lpush(Constants.REDIS_KEY_QUEUE_VIDEO_PLAY, videoPlayInfoDto, null);
    }
}