package com.fondant.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class PageConfig implements WebMvcConfigurer {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    @Bean
    public Pageable defaultPageable() {
        return PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
        resolver.setFallbackPageable(PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE));
        resolver.setMaxPageSize(MAX_SIZE);
        resolvers.add(resolver);
    }

    public Pageable customPageable(int page) {
        return PageRequest.of(page, DEFAULT_SIZE);
    }
}