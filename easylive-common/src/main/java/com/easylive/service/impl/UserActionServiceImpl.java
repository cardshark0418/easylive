package com.easylive.service.impl;

import com.easylive.entity.po.UserAction;
import com.easylive.mapper.UserActionMapper;
import com.easylive.service.UserActionService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserActionServiceImpl extends MPJBaseServiceImpl<UserActionMapper, UserAction> implements UserActionService {
}