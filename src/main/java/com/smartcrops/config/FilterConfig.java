package com.smartcrops.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.smartcrops.jwt.JwtFilter;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilter() {

        FilterRegistrationBean<JwtFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new JwtFilter());
        bean.addUrlPatterns("/api/*");

        return bean;
    }
}
