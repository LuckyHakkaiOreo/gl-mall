package com.winster.glmall.glmallware.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * spring上下文工具类
 */
public class SpringApplicationContextHolder {
    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringApplicationContextHolder.applicationContext = applicationContext;
    }

    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
