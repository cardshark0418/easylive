package com.easylive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.easylive"})
public class EasyliveWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyliveWebApplication.class, args);
    }

}
