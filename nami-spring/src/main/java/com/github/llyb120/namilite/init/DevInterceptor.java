package com.github.llyb120.namilite.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.github.llyb120.namilite.hotswap.SpringHotSwap.compileTask;
import static com.github.llyb120.namilite.hotswap.SpringHotSwap.springReloadTask;

/**
 * @Author: Administrator
 * @Date: 2020/8/1 1:03
 */
@Configuration
public class DevInterceptor implements WebMvcConfigurer {

    @Autowired
    NamiLite namiLite;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if(!namiLite.isDev()){
            return;
        }
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                if(springReloadTask != null){
                    springReloadTask.get();
                }
                return true;
            }
        }).addPathPatterns("/**");
    }

}
