package com.easylive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easylive.entity.po.UserInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}
