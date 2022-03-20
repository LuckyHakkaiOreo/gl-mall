package com.winster.glmall.glmallorder.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Configuration
public class GlmallFeignConfig {

    /**
     * 这里注册到ioc容器中的拦截器RequestInterceptor，会被feign的底层从ioc容器中获取出来
     * 然后添加到feign.Feign.Builder#requestInterceptors列表中
     *
     * @return
     */
    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                log.info("{}-当前线程：{}", "RequestInterceptor", Thread.currentThread().getName());

                // 从spring中获取当前请求
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                // 将请求头的cookie同步到新请求
                HttpServletRequest request = requestAttributes.getRequest();
                template.header("Cookie", request.getHeader("Cookie"));
            }
        };
    }
}
