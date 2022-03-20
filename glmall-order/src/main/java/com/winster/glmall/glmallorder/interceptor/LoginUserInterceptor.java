package com.winster.glmall.glmallorder.interceptor;

import com.alibaba.fastjson.JSON;
import com.winster.common.constant.AuthServerConstant;
import com.winster.common.to.MemberLoginedRes;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<MemberLoginedRes> loginedResThreadLocal = new ThreadLocal<>();
    public static final ThreadLocal<String> idThreadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String requestURL = request.getRequestURL().toString();
        if (requestURI.contains("glmallOrder/")) {
            return true;
        }
        //        Object attribute = request.getSession().getAttribute(AuthServerConstant.LOGIN_USER_SESSION);
        MemberLoginedRes member = JSON.parseObject(JSON.toJSONString(request.getSession().getAttribute(AuthServerConstant.LOGIN_USER_SESSION)), MemberLoginedRes.class);


        if (member == null) {
            // 用户没登录，需要重定向到登录页面
            request.getSession().setAttribute("loginErr", "请先登录");
            response.sendRedirect("http://auth.glmall.com/login.html");
        }

        loginedResThreadLocal.set(member);
        idThreadLocal.set(Thread.currentThread().getName() + "-->" + Thread.currentThread().getId());

        return true;
    }
}
