package com.github.llyb120.namilite.init;

import com.github.llyb120.namilite.hotswap.SpringHotSwap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.github.llyb120.namilite.hotswap.SpringHotSwap.compileTask;
import static com.github.llyb120.namilite.hotswap.SpringHotSwap.springReloadTask;

/**
 * @Author: Administrator
 * @Date: 2020/8/1 1:03
 */
@Component
@Order(-1)
public class DevInterceptor implements Filter {

    @Autowired
    NamiLite namiLite;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(namiLite.isDev()){
            HttpServletRequest req = (HttpServletRequest) request;
            try {
                SpringHotSwap.lock.lock();
                SpringHotSwap.condition.signalAll();
                SpringHotSwap.resCondition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                SpringHotSwap.lock.unlock();
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
