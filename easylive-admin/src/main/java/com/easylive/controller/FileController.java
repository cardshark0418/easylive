package com.easylive.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.easylive.config.AppConfig;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.enums.DateTimePatternEnum;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.Date;

@Validated
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {
    @Autowired
    private AppConfig appConfig;

    @RequestMapping("/uploadImage")
    public ResponseVO uploadCover(@NotNull MultipartFile file, @NotNull Boolean createThumbnail) throws IOException {
        String month = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMM.getPattern());
        String folder = appConfig.getProjectFolder() + "file/" + "cover/" + month;
        File folderFile = new File(folder);
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }
        String fileName = file.getOriginalFilename();
        String fileSuffix = fileName.substring(fileName.lastIndexOf("."));
        String realFileName = RandomUtil.randomString(30) + fileSuffix;
        String filePath = folder + "/" + realFileName;
        file.transferTo(new File(filePath));
        if (createThumbnail) {
            //生成缩略图
            String thumbPath = filePath + Constants.IMAGE_THUMBNAIL_SUFFIX;
            ImgUtil.scale(
                    FileUtil.file(filePath),
                    FileUtil.file(thumbPath),
                    200,
                    -1,
                    null
            );
        }
        return ResponseVO.getSuccessResponseVO( "cover/"+ month + "/" + realFileName);
    }

    @RequestMapping("/getResource")
    public void getResource(HttpServletResponse response, @NotEmpty String sourceName) {
        // 1. 安全校验：检查路径是否合法（防止 ../../ 攻击）
        if (StrUtil.contains(sourceName, "..") || StrUtil.isBlank(sourceName)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // 2. 拼接绝对路径
        // 使用 FileUtil.file 组合路径，它会自动处理多余的斜杠
        String fullPath = appConfig.getProjectFolder() + "file/" + sourceName;
        File file = FileUtil.file(fullPath);

        // 3. 检查文件是否存在
        if (!FileUtil.exist(file)) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }

        // 4. 设置缓存（30天）
        response.setHeader("Cache-Control", "max-age=2592000");

        // 5. 【核心平替】一行代码完成：读取文件、判断 Content-Type、写入响应流
        // 它会根据文件后缀（.png, .jpg等）自动设置 response.setContentType
        ServletUtil.write(response, file);
    }
}
