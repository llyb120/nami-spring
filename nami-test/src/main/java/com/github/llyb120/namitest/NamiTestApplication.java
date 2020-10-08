package com.github.llyb120.namitest;

import com.github.llyb120.namilite.hotswap.Refresh;
import com.github.llyb120.namitest.confi.TestBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

@SpringBootApplication
public class NamiTestApplication {

    @Refresh
    @Bean(name = "testBean")
    public TestBean test(){
        return new TestBean();
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(NamiTestApplication.class, args);
        TestBean testBean = (TestBean) ctx.getBean("testBean");
        testBean.test();
        testBean = (TestBean) ctx.getBean("testBean");
        testBean.test();
//        SpringApplication.run(NamiTestApplication.class, args);
    }


//    @Bean
//    public NamiConfig namiConfig(){
//        return new NamiConfig(){
//
//            //注册热加载的包
//            @Override
//            public List<String> hotPackages() {
//                return a(
//                ) ;
//            }
//
//            @Override
//            public List<String> springHotPackages() {
//                return a();
////                return a(
////                    "com.github.llyb120.namitest.test"
////                );
//            }
//
//            //0.0.8 新增，现在支持对多个路由和包的映射
//            @Override
//            public Map controlPackages() {
//                return o(
//                    "/api/:c/:a", "com.github.llyb120.stock.ctrl"
//                );
//            }
//        } ;
//    }

}
