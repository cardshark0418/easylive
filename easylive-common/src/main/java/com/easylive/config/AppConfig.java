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

    @Getter
    @Value("${showFFmegLog:true}")
    private Boolean showFFmpegLog;

    @Value("${es.host.port:127.0.0.1:9200}")
    private String esHostPort;

    @Value("${es.index.video.name:easylive_video}")
    private String esIndexVideoName;
}


