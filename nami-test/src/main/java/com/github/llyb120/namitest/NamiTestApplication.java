package com.github.llyb120.namitest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NamiTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(NamiTestApplication.class, args);
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
