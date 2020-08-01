package com.github.llyb120.namitest.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.github.llyb120.namitest.test.TestService;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author: Administrator
 * @Date: 2020/7/30 21:49
 */
@RestController
@RequestMapping("/")
public class TestController {
    public int d = 4;

    @Value("${a}")
    String ttt;

    @Resource
    TestService testService;

    @Resource
    TService tService;

    @RequestMapping("/abc")
    public Object ttt(String a, String b) throws InvocationTargetException, IllegalAccessException {
        Test.test();//////dfdfd
        testService.test();
//        Object testService = context.getBean("testService");
//        for (Method declaredMethod : testService.getClass().getDeclaredMethods()) {
//            declaredMethod.setAccessible(true);
//            declaredMethod.invoke(testService);
//        }
        return tService.foo();
//        testService.test();
//        return "fuck-" + a + "-" + b + "-" + ttt;
    }

    @RequestMapping("/shit")
    public Object test2(){
        return "im not shit";
    }

}
