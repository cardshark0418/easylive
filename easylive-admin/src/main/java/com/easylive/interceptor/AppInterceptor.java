package com.easylive.interceptor;

import cn.hutool.core.util.StrUtil;
import com.easylive.entity.constants.Constants;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.redis.RedisUtils;
import com.easylive.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AppInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisUtils redisUtils;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //如果请求静态资源 直接放行
        if(!(handler instanceof HandlerMethod)){
            return true;
        }
        //放行account
        if(request.getRequestURI().contains("/account")){
            return true;
        }
        String token = CookieUtil.getCookieToken(request);
        if(StrUtil.isBlank(token)) throw new BusinessException(ResponseCodeEnum.CODE_901);

        Object o = redisUtils.get(Constants.REDIS_KEY_ADMIN_TOKEN + token);
        if(o==null) throw new BusinessException(ResponseCodeEnum.CODE_901);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}

