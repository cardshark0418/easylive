package com.easylive.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class AppConfig {
    @Value("${admin.account:admin_default}")
    public String account;

    @Value("${admin.password:pw_default}")
    public String password;

    @Value("${project.folder:}")
    private String projectFolder;

    @Value("${showFFmegLog:true}")
    private Boolean showFFmpegLog;

    public Boolean getShowFFmpegLog() {
        return showFFmpegLog;
    }
}
