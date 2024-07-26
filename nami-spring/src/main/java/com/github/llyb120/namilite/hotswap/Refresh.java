package com.github.llyb120.namilite.hotswap;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import java.lang.annotation.*;

/**
 * @Author: Administrator
 * @Date: 2020/10/8 16:53
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Scope("refreshScope")
@Documented
public @interface Refresh {
    ScopedProxyMode proxyMode() default ScopedProxyMode.TARGET_CLASS;
}

