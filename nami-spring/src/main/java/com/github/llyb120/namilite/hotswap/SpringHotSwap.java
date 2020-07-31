package com.github.llyb120.namilite.hotswap;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.IdUtil;
import com.github.llyb120.json.Json;
import com.github.llyb120.namilite.core.Async;
import com.github.llyb120.namilite.init.NamiLite;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

import static com.github.llyb120.namilite.init.NamiBean.namiConfig;

/**
 * @Author: Administrator
 * @Date: 2020/7/31 15:57
 */
public class SpringHotSwap {

    public volatile static Future compileTask = null;
    private static NamiHotLoader namiHotLoader = new NamiHotLoader();
    private static SpringHotLoader springHotLoader = new SpringHotLoader();
//    public static ReentrantLock lock = new ReentrantLock();
    private static Set<File> changedFile = new ConcurrentHashSet<>();

    private static Set<File> getSpringHotFiles() {
        Set<File> set = new HashSet<>();
        for (String springHotPackage : namiConfig.springHotPackages()) {
            try {
                Files.walkFileTree(Paths.get(NamiHotLoader.src + "/" + springHotPackage.replaceAll("\\.", "/")), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        set.add(file.toFile());
                        return super.visitFile(file, attrs);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return set;
    }

    public static void refreshSpringBeans(ApplicationContext context) {
        Set<File> springFiles = getSpringHotFiles();
        if(springFiles.isEmpty()){
            return;
        }
        RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) context.getBean("requestMappingHandlerMapping");
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();

        //对spring进行热加载
        SpringHotLoader loader = new SpringHotLoader();
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
                    (e.getAnnotation(RequestMapping.class) != null || e.getAnnotation(Service.class) != null);
            })
            .sorted(new Comparator<Class<?>>() {
                @Override
                public int compare(Class<?> o1, Class<?> o2) {
                    if (o1.getAnnotation(Service.class) != null) {
                        return -1;
                    } else if (o1.getAnnotation(RequestMapping.class) != null) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            })
            .forEachOrdered(e -> {
                if (e.getAnnotation(Service.class) != null) {
                    reRegisterService(context, defaultListableBeanFactory, e);
                } else {
                    reRegisterController(context, requestMappingHandlerMapping, defaultListableBeanFactory, e);
                }
            });
    }

    public static void startCompile(ApplicationContext context, File file) {
        if (compileTask == null) {
            compileTask = Async.execute(() -> {
                try {
                    Thread.sleep(66);
                    File[] files = changedFile.stream()
                        .filter(e -> namiHotLoader.isHotFile(e) || springHotLoader.isHotFile(e))
                        .toArray(File[]::new);
                    NamiHotLoader.compile(
                        files
                    );
                    refreshSpringBeans(context);
                    System.out.println("compiled and reload success");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    changedFile.clear();
                    compileTask = null;
                }
            });
        }
        changedFile.add(file);
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
            String ctrlName = ctrlNames.get(clz.getSimpleName());
            if (ctrlName == null) {
                ctrlName = clz.getSimpleName().substring(0, 1).toLowerCase() + clz.getSimpleName().substring(1);
            }
            if (context.containsBean(ctrlName)) {
                try {
                    BeanDefinition definition = defaultListableBeanFactory.getBeanDefinition(ctrlName);
                    Class targetClass = context.getClassLoader().loadClass(definition.getBeanClassName());
                    ReflectionUtils.doWithMethods(targetClass, new ReflectionUtils.MethodCallback() {
                        @Override
                        public void doWith(Method method) {
                            Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
                            try {
                                Method createMappingMethod = RequestMappingHandlerMapping.class.
                                    getDeclaredMethod("getMappingForMethod", Method.class, Class.class);
                                createMappingMethod.setAccessible(true);
                                RequestMappingInfo requestMappingInfo = (RequestMappingInfo)
                                    createMappingMethod.invoke(requestMappingHandlerMapping, specificMethod, targetClass);
                                if (requestMappingInfo != null) {
                                    requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, ReflectionUtils.USER_DECLARED_METHODS);

                    defaultListableBeanFactory.removeBeanDefinition(ctrlName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //生成新类名
            String clzFakeName = "namiDev_" + IdUtil.objectId();
            ctrlNames.put(clz.getSimpleName(), clzFakeName);
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
