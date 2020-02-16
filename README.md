# nami-spring
以插件化的形式注入spring，在不影响spring原功能的情况下，为srpingboot提供高效的热加载开发能力，以及灵活的数据可变性

## 快速开始
1 新建一个空的springboot项目

2 加入依赖
```xml
<dependency>
    <groupId>com.github.llyb120</groupId>
    <artifactId>nami-spring</artifactId>
    <version>LATEST</version>
</dependency>
```

3 注册一个NamiConfig的Bean
```java
@Bean
public NamiConfig namiConfig(){
    return new NamiConfig(){
        //注册热加载的包
        @Override
        public String[] hotPackages() {
            return new String[]{
                "com.github.llyb120.stock.ctrl"
            };
        }

        //注册控制器所在包
        @Override
        public String controllerPackage() {
            return "com.github.llyb120.stock.ctrl";
        }
    } ;
}
```
4 注册控制器
```java
@RequestMapping("/api")
@RestController
public class Ctrl extends NamiSpringController {
}
``` 

至此，即可编写Nami的控制器，并享受Nami带来的飞速开发 

## spring对象的注入
Nami使用import static来获取bean，故不会侵入spring原本的注入方式，只需建立一个类注入即可
```java
public class TestBean {

    public static Environment environment;
    public static File dir;

    @Autowired
    public void set(@Autowired Environment env){
        environment = env;
        dir = new File(environment.getProperty("stock.path"));
    }
}
```
