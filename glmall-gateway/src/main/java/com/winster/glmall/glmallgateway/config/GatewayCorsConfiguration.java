package com.winster.glmall.glmallgateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class GatewayCorsConfiguration {

    @Bean
    public CorsWebFilter corsWebFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedHeader("*"); // 允许哪些请求头进行跨域配置，默认只有6个基本字段，如果任何请求头都允许请配置：*
        corsConfiguration.addAllowedMethod("*");// 允许哪些方法跨域，默认仅支持简单方法：get、post、put，但是还是和请求头数据有关系
        corsConfiguration.addAllowedOrigin("*");// 允许那些请求来源进行跨域，如果任何请求来源都允许请配置：*
        corsConfiguration.setAllowCredentials(true);// 是否允许请求携带cookie：true/false

        // 针对所有的请求路径，都使用这个跨域配置（corsConfiguration）
        source.registerCorsConfiguration("/**", corsConfiguration);
        CorsWebFilter corsWebFilter = new CorsWebFilter(source);
        return corsWebFilter;
    }
}
