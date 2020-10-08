package com.github.llyb120.namilite.hotswap;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import com.github.llyb120.namilite.core.Async;
import com.github.llyb120.namilite.init.NamiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.github.llyb120.namilite.init.NamiBean.namiConfig;

/**
 * @Author: Administrator
 * @Date: 2020/7/31 15:57
 */
@Service
public class SpringHotSwap {
    @Autowired
    NamiProperties namiProperties;
    @Autowired
    ApplicationContext context;

    public volatile static Future compileTask = null;
    public volatile static Future springReloadTask = null;
    private static NamiHotLoader namiHotLoader = new NamiHotLoader();
    private static SpringHotLoader springHotLoader = new SpringHotLoader();
    //    public static ReentrantLock lock = new ReentrantLock();
    private static volatile Set<File> changedFile = new ConcurrentHashSet<>();
    private static volatile Set<File> lastErrorFiles = new ConcurrentHashSet<>();

    public static ReentrantLock lock = new ReentrantLock();
    public static Condition condition = lock.newCondition();
    public static Condition resCondition = lock.newCondition();


    private Set<File> getSpringHotFiles(boolean onlyReturnChanged) {
        Set<File> set = new HashSet<>();
        Set<String> removeAble = new HashSet<>();
        List<String> pkgs = namiProperties.getSpringHotPackages();
        if (pkgs.isEmpty()) {
            pkgs = namiConfig.springHotPackages();
        }
        for (String springHotPackage : pkgs) {
            if (springHotPackage.startsWith("!")) {
                removeAble.add(new File(springHotPackage.substring(1)).getAbsolutePath());
                continue;
            }
            try {
                Files.walkFileTree(Paths.get(NamiHotLoader.src + "/" + springHotPackage.replaceAll("\\.", "/")), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        File _file = file.toFile();
                        if (_file.getName().contains("~")) {
                            _file = new File(_file.getAbsolutePath().replaceAll("~", ""));
                        }
                        if(onlyReturnChanged){
                            //得到编译后的文件
                            File targetFile = new File(_file.getAbsolutePath()
                                .replace(NamiHotLoader.src, NamiHotLoader.target)
                                .replaceAll("\\.java", ".class"));
                            if(!targetFile.exists() || _file.lastModified() > targetFile.lastModified()){
                                set.add(_file);
                            }
                        } else {
                            set.add(_file);
                        }
                        return super.visitFile(file, attrs);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Iterator<File> it = set.iterator();
        while (it.hasNext()) {
            File item = it.next();
            String path = item.getAbsolutePath();
            for (String s : removeAble) {
                if (s.contains(path)) {
                    it.remove();
                    break;
                }
            }
        }
        return set;
    }

    public void refreshSpringBeans(Set<File> springFiles) {
        RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) context.getBean("requestMappingHandlerMapping");
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();

        //对spring进行热加载
        SpringHotLoader loader = new SpringHotLoader();
        //卸载所有符合条件的控制器
        unloadControllers(context, requestMappingHandlerMapping, defaultListableBeanFactory);
        springFiles.stream()
            .map(e -> {
                try {
                    return loader.loadClass(NamiHotLoader.toClassName(e));
                } catch (ClassNotFoundException classNotFoundException) {
                    classNotFoundException.printStackTrace();
                    return null;
                }
            })
            .filter(e -> {
                return e != null &&
                    (e.getAnnotation(RequestMapping.class) != null || e.getAnnotation(Service.class) != null); //|| e == Filter.class);
            })
//            .sorted(new Comparator<Class<?>>() {
//                @Override
//                public int compare(Class<?> o1, Class<?> o2) {
//                    if (o1.getAnnotation(Service.class) != null) {
//                        return -1;
//                    } else if (o1.getAnnotation(RequestMapping.class) != null) {
//                        return 1;
//                    } else {
//                        return 0;
//                    }
//                }
//            })
            .forEachOrdered(e -> {
//                if(e == Filter.class){
//                    reRegisterService(context, defaultListableBeanFactory, e);
//                } else
                if (e.getAnnotation(Service.class) != null) {
                    reRegisterService(context, defaultListableBeanFactory, e);
                } else {
                    reRegisterController(context, requestMappingHandlerMapping, defaultListableBeanFactory, e);
                }
            });
    }

    public void refreshSpringBeans() {
        Set<File> springFiles = getSpringHotFiles(false);
        if (springFiles.isEmpty()) {
            return;
        }
        refreshSpringBeans(springFiles);

    }

    @Deprecated
    public static void startCompile(ApplicationContext context, File file) {
        if (compileTask == null) {
            compileTask = Async.execute(() -> {
                try {
                    Thread.sleep(66);
                    File[] files = changedFile.stream()
                        .filter(namiHotLoader::isHotFile)
                        .toArray(File[]::new);
                    NamiHotLoader.compile(
                        files
                    );
//                    System.out.println("compiled and reload success");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    changedFile.clear();
                    compileTask = null;
                }
            });
        }

//        if (springReloadTask == null) {
//            springReloadTask = Async.execute(() -> {
//                try {
//                    Thread.sleep(50);
//                    HashSet<File> cpSet = new HashSet<>(changedFile);
//                    if(
//                        cpSet.stream().noneMatch(e -> springHotLoader.isHotFile(e))
//                    ){
//                        return;
//                    }
//                    refreshSpringBeans(context);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } finally {
//                    springReloadTask = null;
//                }
//            });
//        }
        changedFile.add(file);
    }

    public void startCompile2() throws ExecutionException, InterruptedException {
        try{
            Set<File> files = getSpringHotFiles(true);
            if(!lastErrorFiles.isEmpty()){
                files.addAll(
                    //只保留存在的文件
                    lastErrorFiles.stream()
                    .filter(File::exists)
                    .collect(Collectors.toList())
                );
                lastErrorFiles.clear();
            }
            if(files.isEmpty()){
                return;
            }
//            System.out.println(files);
            List<File> failed = NamiHotLoader.compileSync(ArrayUtil.toArray(files, File.class));
            if(!failed.isEmpty()){
                //编译失败
                lastErrorFiles.clear();
                lastErrorFiles.addAll(failed);
                return;
            }
            refreshSpringBeans();
//            System.out.println("bean refreshed");
        } finally {
            resCondition.signalAll();
//            System.out.println("res notifyed");
        }
    }

    private static void unloadControllers(ApplicationContext context, RequestMappingHandlerMapping requestMappingHandlerMapping, DefaultListableBeanFactory defaultListableBeanFactory){
        Method getMappingForMethod =ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getMappingForMethod",Method.class,Class.class);
        //设置私有属性为可见
        getMappingForMethod.setAccessible(true);
        for (String s : defaultListableBeanFactory.getBeanNamesForAnnotation(RequestMapping.class)) {
            Object bean = defaultListableBeanFactory.getBean(s);
            if(!springHotLoader.isHotClass(bean.getClass().getName())){
                continue;
            }
            for (Method method : bean.getClass().getDeclaredMethods()) {
                if(method.getAnnotation(RequestMapping.class) != null || method.getAnnotation(GetMapping.class) != null || method.getAnnotation(PostMapping.class) != null){
                    RequestMappingInfo mappingInfo = null;
                    try {
                        mappingInfo = (RequestMappingInfo) getMappingForMethod.invoke(requestMappingHandlerMapping, method,bean.getClass());
                        requestMappingHandlerMapping.unregisterMapping(mappingInfo);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
            try{
                defaultListableBeanFactory.removeBeanDefinition(s);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static void reRegisterService(ApplicationContext context, DefaultListableBeanFactory defaultListableBeanFactory, Class clz) {
        String ctrlName = clz.getSimpleName().substring(0, 1).toLowerCase() + clz.getSimpleName().substring(1);
        if (context.containsBean(ctrlName)) {
            try {
                defaultListableBeanFactory.removeBeanDefinition(ctrlName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BeanDefinitionBuilder bean = BeanDefinitionBuilder.genericBeanDefinition(clz);
        defaultListableBeanFactory.registerBeanDefinition(ctrlName, bean.getBeanDefinition());
    }

    private static void reRegisterController(ApplicationContext context, RequestMappingHandlerMapping requestMappingHandlerMapping, DefaultListableBeanFactory defaultListableBeanFactory, Class clz) {
        try {
            String ctrlName = null;//ctrlNames.get(clz.getSimpleName());
//            if (ctrlName == null) {
                ctrlName = clz.getSimpleName().substring(0, 1).toLowerCase() + clz.getSimpleName().substring(1);
//            }
//            if (context.containsBean(ctrlName)) {
//                try {
//                    BeanDefinition definition = defaultListableBeanFactory.getBeanDefinition(ctrlName);
//                    Class targetClass = context.getClassLoader().loadClass(definition.getBeanClassName());
//                    ReflectionUtils.doWithMethods(targetClass, new ReflectionUtils.MethodCallback() {
//                        @Override
//                        public void doWith(Method method) {
//                            Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
//                            try {
//                                Method createMappingMethod = RequestMappingHandlerMapping.class.
//                                    getDeclaredMethod("getMappingForMethod", Method.class, Class.class);
//                                createMappingMethod.setAccessible(true);
//                                RequestMappingInfo requestMappingInfo = (RequestMappingInfo)
//                                    createMappingMethod.invoke(requestMappingHandlerMapping, specificMethod, targetClass);
//                                if (requestMappingInfo != null) {
//                                    requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }, ReflectionUtils.USER_DECLARED_METHODS);
//
//                    defaultListableBeanFactory.removeBeanDefinition(ctrlName);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            //生成新类名
            String clzFakeName = "namiDev_" + IdUtil.objectId();
//            ctrlNames.put(clz.getSimpleName(), clzFakeName);
            // 这里通过builder直接生成了mycontrooler的definition，然后注册进去
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clz);
//                                                beanDefinitionBuilder.addPropertyValue("testService", context.getBean("testService"));
            defaultListableBeanFactory.registerBeanDefinition(clzFakeName, beanDefinitionBuilder.getBeanDefinition());
            Method method = requestMappingHandlerMapping.getClass().getSuperclass().getSuperclass().getDeclaredMethod("detectHandlerMethods", Object.class);
            method.setAccessible(true);
            method.invoke(requestMappingHandlerMapping, clzFakeName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> ctrlNames = new HashMap<>();
}
