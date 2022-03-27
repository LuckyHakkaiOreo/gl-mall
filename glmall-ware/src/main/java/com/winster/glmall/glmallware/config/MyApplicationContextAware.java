package com.winster.glmall.glmallware.config;

import com.winster.glmall.glmallware.utils.SpringApplicationContextHolder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


@Component
public class MyApplicationContextAware implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        SpringApplicationContextHolder.setApplicationContext(applicationContext);
    }

    public Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
