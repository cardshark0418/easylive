package com.easylive.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.redis.RedisUtils;
import com.easylive.service.UserInfoService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static com.easylive.entity.vo.ResponseVO.getSuccessResponseVO;

@RestController
@RequestMapping("/user")
@Validated
public class UserController{
    @Resource
    private UserInfoService userInfoService;
    @Autowired
    private RedisUtils redisUtils;

    @RequestMapping("/loadUser")
    public ResponseVO loadUser(Integer pageNo,String nickNameFuzzy,Integer status) {
        pageNo= pageNo==null?1:pageNo;
        Page<UserInfo> page = userInfoService.selectJoinListPage(new Page<UserInfo>(pageNo, 15), UserInfo.class, new MPJLambdaWrapper<UserInfo>()
                .orderByDesc(UserInfo::getJoinTime)
                .eq(status != null, UserInfo::getStatus, status)
                .like(!StringUtils.isEmpty(nickNameFuzzy), UserInfo::getNickName, nickNameFuzzy));
        return getSuccessResponseVO(new PaginationResultVO<>((int) page.getTotal(),15,pageNo,page.getRecords()));
    }

    @RequestMapping("/changeStatus")
    public ResponseVO changeStatus(String userId, Integer status) {
        UserInfo userInfo = new UserInfo();
        userInfo.setStatus(status);
        userInfo.setUserId(userId);
        userInfoService.updateById(userInfo);
        if(status==0){
            String token = (String) redisUtils.get(Constants.REDIS_KEY_USER_TOKEN + userId);
            if(StrUtil.isEmpty(token)) return getSuccessResponseVO(null);
            redisUtils.delete(Constants.REDIS_KEY_USER_TOKEN + userId);
            redisUtils.delete(Constants.REDIS_KEY_LOGIN_TOKEN + token);
        }
        return getSuccessResponseVO(null);
    }
}
