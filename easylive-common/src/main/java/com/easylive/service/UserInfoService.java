package com.easylive.service;

import com.easylive.entity.vo.UserLoginDto;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

public interface UserInfoService {
    void register(String email, String nickName, String password);

    UserLoginDto login(@NotEmpty @Email String email, @NotEmpty String password, String ip, HttpServletResponse response);
}
