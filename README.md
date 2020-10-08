# nami-spring
以插件化的形式注入spring，在不影响spring原功能的情况下，为srpingboot提供高效的热加载开发能力，以及灵活的数据可变性

## 快速开始

```xml
<dependency>
    <groupId>com.github.llyb120</groupId>
    <artifactId>nami-spring</artifactId>
    <version>LATEST</version>
</dependency>
```

## 针对Spring的热加载
自0.0.20起，可以用!表示被排除热加载的包，例如同时具有com.demo.test和!com.demo.test.Test，则表示test包下的类全部热加载，但是排除Test类

自0.0.17起，已经可以支持spring component的热加载，需满足以下条件
* 被加载的组件必须有 @RequestMapping 或 @Service 注解
* 被加载的组件中不能出现 @Bean 等初始化的东西
* 如果需要 @Autowired 注入，则需要使用 @Resource 代替
* 被加载的组件的包或者父包必须在 nami.spring-hot-packages中声明

## 针对固定组件的热加载
自0.0.26起，可以对bean的初始化方法增加@Refresh注解，当触发热加载的时候，会通知spring重新加载这个bean（通常用于一些需要重新初始化的bean，例如beetlSQL）

## 针对Lombok的热加载
自0.0.28起，可以配置nami.compiler进行编译器的切换，在使用javac的时候，可以同时开启nami.lombok=true，此时无需再配置javaagent

自0.0.19起，已经可以支持对lombok的热加载
* 启动时需要加上 -javaagent:lib/lombok.jar=ECJ 
* lombok.jar的版本需要和pom中引入的lombok版本一致，推荐1.16.10，也可以直接下本项目中lib/lombok.jar来使用

