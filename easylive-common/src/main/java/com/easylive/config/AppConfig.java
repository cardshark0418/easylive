package com.easylive.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class AppConfig {
    @Value("${admin.account}")
    public String account;

    @Value("${admin.password}")
    public String password;

}
