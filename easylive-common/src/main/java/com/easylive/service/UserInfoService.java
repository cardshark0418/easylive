package com.easylive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.vo.UserLoginDto;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

public interface UserInfoService extends IService<UserInfo> {
    void register(String email, String nickName, String password);

    UserLoginDto login(@NotEmpty @Email String email, @NotEmpty String password, String ip, HttpServletResponse response);

    UserInfo getUserDetailInfo(String s, @NotEmpty String userId);

    void updateUserInfo(UserInfo userInfo, UserLoginDto tokenUserInfoDto);
}
