package com.winster.glmall.cart.interceptor;

import com.alibaba.fastjson.JSON;
import com.winster.common.constant.AuthServerConstant;
import com.winster.common.constant.CartConstant;
import com.winster.common.to.MemberLoginedRes;
import com.winster.glmall.cart.vo.UserInfoTo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 执行目标方法之前，判断用户的登录状态：
 * 是临时用户还是登录用户
 */
@Component
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     * 在目标方法执行之前拦截
     *
     * @param request
     * @param response
     * @param handler
     * @return true，放行目标方法；false，不放行
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo to = new UserInfoTo();
        HttpSession session = request.getSession();
        MemberLoginedRes member = JSON.parseObject(JSON.toJSONString(session.getAttribute(AuthServerConstant.LOGIN_USER_SESSION)), MemberLoginedRes.class);

        if (member != null) {
            // 用户已登录
            to.setUserId(member.getId());
        }
        // 从请求中，获取user-key
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (CartConstant.TEMP_USER_COOKIE_NAME.equals(name)) {
                    to.setUserKey(cookie.getValue());
                    to.setTempUser(true);
                }
            }
        }
        // 如果用户是第一次登录，自定义一个临时用户
        if (StringUtils.isBlank(to.getUserKey())) {
            String s = UUID.randomUUID().toString();
            to.setUserKey(s);
        }

        // 目标方法执行之前，将保存to
        threadLocal.set(to);

        return true;
    }

    /**
     * 目标方法执行之后
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo to = threadLocal.get();
        // 如果请求进来的时候用户没有带临时用户cookie，则生成一个cookie
        if (!to.getTempUser()) {
            Cookie[] cookies = request.getCookies();
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, to.getUserKey());
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            cookie.setDomain("glmall.com");
            response.addCookie(cookie);
        }
        // 清空当前登录的用户数据
        threadLocal.remove();
    }
}
