package com.github.llyb120.namitest.test;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import javax.servlet.*;
import java.io.IOException;

/**
 * @Author: Administrator
 * @Date: 2020/8/2 13:01
 */
@Component
public class TestFilter implements Filter {

    @Resource
    WebApplicationContext context;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("fuck hit");

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
