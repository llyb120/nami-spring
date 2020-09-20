package com.github.llyb120.namitest.test;

import com.github.llyb120.namilite.hotswap.SpringHotSwap;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import javax.servlet.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Administrator
 * @Date: 2020/8/2 13:01
 */
@Component
@Order(-1)
public class TestFilter implements Filter {

    @Resource
    WebApplicationContext context;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
