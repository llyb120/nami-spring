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

## 控制器(如无特殊说明，所有控制器均指Nami的控制器而非spring)
* 你可以在非Nami控制器包下使用spring原本的控制器
* 控制器的类必须继承 NamiBaseController ，例如
```java
public class Test extends NamiBaseController {
}
```
* 若配置了访问路径(例如上文的/api)以及控制器的包名(例如com.example.ctrl)，则访问/api/{c}/{a}时会自动调用com.example.ctrl.{c}.{a}方法，a必须为一个public无参实现

## 依赖注入
* 对于spring自身的东西，你仍可以按照以前的方式使用
* 对于nami，使用import static来获取bean，故不会侵入spring原本的注入方式，只需建立一个类注入即可
```java
//TestBean.java 注：该类不能放在热加载的包内
@Component
public class TestBean {

    public static Environment environment;
    public static File dir;

    @Autowired
    public void set(@Autowired Environment env){
        environment = env;
        dir = new File(environment.getProperty("stock.path"));
    }
}

//test.java
import static TestBean.*;
public class test extends NamiBaseController{
    public String test(){
        return environment.getProperty("stock.path");
    }   
}
```

## 热加载
* 如果你使用了nami的控制器，并且所调用的内容在配置的热加载包下，那么每当你修改这些内容的时候，无需重启服务直接刷新即可看到效果
* 通常，你注入的spring的内容不要放在热加载的内容中，否则会无法正确获取到bean

## 获取参数
在控制器中你可以使用类似php的 $get/$post/$request/$files 来获取请求的值，分别对应GET请求/POST请求/任何请求/上传的文件

## 授权
* 你可以使用原本spring的过滤器来实现授权
* 如果你想，你可以在NamiConfig中重写namiAuth来提供一个用于授权的对象, nami会在所有的请求中验证授权，如果你不需要，请使用UnLogin注解
