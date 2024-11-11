package com.ucemucar.load_shedding.configuration;

import com.ucemucar.load_shedding.filter.LoadSheddingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfiguration {
    @Bean
    public FilterRegistrationBean<LoadSheddingFilter> loggingFilter() {
        FilterRegistrationBean<LoadSheddingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new LoadSheddingFilter());
        registrationBean.addUrlPatterns("/hello"); // Filter only applies to /hello endpoint
        return registrationBean;
    }
}