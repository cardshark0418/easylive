package com.easylive.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.easylive.entity.constants.Constants;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.vo.UserLoginDto;
import com.easylive.exception.BusinessException;
import com.easylive.mapper.UserInfoMapper;
import com.easylive.redis.RedisUtils;
import com.easylive.service.UserInfoService;
import com.easylive.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.UUID;

@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RedisUtils redisUtils;

    @Override
    public void register(String email, String nickName, String password) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getEmail,email);
        long count = userInfoMapper.selectCount(wrapper);
        if(count>0){
            throw new BusinessException("该邮件已被注册");
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setEmail(email);
        userInfo.setNickName(nickName);
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        userInfo.setPassword(md5Password);
        userInfo.setSex(2);
        userInfo.setJoinTime(LocalDateTime.now());
        userInfo.setTheme(1);
        userInfo.setCurrentCoinCount(10);
        userInfo.setTotalCoinCount(10);
        userInfoMapper.insert(userInfo);
    }

    @Override
    public UserLoginDto login(String email, String password, String ip, HttpServletResponse response) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getEmail,email);
        UserInfo userInfo = userInfoMapper.selectOne(wrapper);
        if(userInfo==null || !userInfo.getPassword().equals(password)){
            throw new BusinessException("邮箱或密码错误");
        }
        if(userInfo.getStatus()==0){
            throw new BusinessException("账号已封禁");
        }
        //更新ip和登录时间
        UserInfo updateInfo = new UserInfo();
        updateInfo.setLastLoginIp(ip);
        updateInfo.setLastLoginTime(LocalDateTime.now());
        userInfoMapper.update(updateInfo,wrapper);
        //构造dto对象
        UserLoginDto userLoginDto = BeanUtil.copyProperties(userInfo, UserLoginDto.class);
        String token = UUID.randomUUID().toString();
        userLoginDto.setExpireAt((System.currentTimeMillis()+Constants.ONE_MIN_MILLS*60*24*7));
        userLoginDto.setToken(token);
        //token存储到redis
        redisUtils.setex(Constants.REDIS_KEY_LOGIN_TOKEN+token,userLoginDto, ((long) Constants.ONE_MIN_MILLS / 1000) *60*24*7);
        //设置cookie
        CookieUtil.setToken2Cookie(response,token);
        return userLoginDto;
    }
}
