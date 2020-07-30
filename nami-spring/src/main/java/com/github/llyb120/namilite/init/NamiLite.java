package com.github.llyb120.namilite.init;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.watch.SimpleWatcher;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.watchers.DelayWatcher;
import cn.hutool.core.util.ZipUtil;

import com.github.llyb120.json.Json;
import com.github.llyb120.namilite.ByteCodeLoader;
import com.github.llyb120.namilite.HotLoader;
import com.github.llyb120.namilite.boost.V20Auto;
import com.github.llyb120.namilite.core.Async;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.Set;

@Configuration
public class NamiLite {
    public static Environment env;
    public static ApplicationContext context;
    public static boolean isDev;
    public static boolean isWin = false;
    public static String jarPath;
    public static File jarDir;
    public static String cp;

    @Autowired
    public void setEnv(Environment _env) {
        env = _env;
    }

    @Autowired
    public void setContext(ApplicationContext _context) {
        context = _context;

        String path = HotLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        isDev = !path.contains(".jar!");

        String osName = System.getProperty("os.name");
        isWin = osName.toLowerCase().startsWith("windows");


        if(!isDev){
            /**
             * 暂时不需要动态编译
             */
            //解压jar包
//            int start = 0;
//            if(path.startsWith("file:/")){
//                if(isWin){
//                    start = "file:/".length();
//                } else {
//                    start = "file:".length();
//                }
//            }
//            int end = path.indexOf(".jar!");
//            end += ".jar".length();
//            File jar = new File(jarPath = path.substring(start, end));
//            jarDir = new File(jar.getParent(), jar.getName().replace(".jar", ""));
//            jarDir.delete();
//            ZipUtil.unzip(jar, jarDir);
//
//            //libpath
//            StringBuilder sb = new StringBuilder();
//            sb.append(new File(jarDir,"BOOT-INF/classes").getAbsolutePath());
//            if(isWin){
//                sb.append(";");
//            } else {
//                sb.append(":");
//            }
//            for (File file : new File(jarDir, "BOOT-INF/lib").listFiles()) {
//                if(file.getName().endsWith(".jar")){
//                    sb.append(file.getAbsolutePath());
//                    if(isWin){
//                        sb.append(";");
//                    } else {
//                        sb.append(":");
//                    }
//                }
//            }
//            if(sb.length() > 0){
//                sb.deleteCharAt(sb.length() - 1);
//            }
//            cp = sb.toString();
//            System.out.println(cp);
        } else {
            watch();
            watchResource();
        }
    }

    private volatile boolean inWatching = false;
    private Set<String> changedFile = new ConcurrentHashSet<>();
    private void watch(){
        WatchMonitor monitor = WatchMonitor.createAll(HotLoader.src, new SimpleWatcher(){
            @Override
            public void onCreate(WatchEvent<?> event, Path currentPath) {
                Path p = (Path) event.context();
                p = Paths.get(currentPath.toString(), p.toString());
                if(Files.isDirectory(p)){
                    return;
                }
                if(!inWatching){
                    inWatching = true;
                    Async.execute(() -> {
                        try {
                            Thread.sleep(100);
                            System.out.println(Json.stringify(changedFile));
                            HotLoader.compile(
                                    changedFile.stream()
                                            .map(File::new)
                                            //过滤掉不热加载的文件
                                            .filter(HotLoader::isHotFile)
                                            .toArray(File[]::new)
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            changedFile.clear();
                            inWatching = false;
                        }
                    });
                }
                String realpath = p.toString();
                if(realpath.endsWith("~")){
                    realpath = realpath.substring(0, realpath.length() - 1);
                }
                changedFile.add(realpath);
            }


        });
        monitor.setMaxDepth(10);
        monitor.start();
        System.out.println(String.format("watch %s to auto compile, target dir is %s", HotLoader.src, HotLoader.target));
    }

    private void watchResource() {
        WatchMonitor monitor = WatchMonitor.createAll(HotLoader.resource, new DelayWatcher(new SimpleWatcher() {
            @Override
            public void onCreate(WatchEvent<?> event, Path currentPath) {
                Path p = (Path) event.context();
                p = Paths.get(currentPath.toString(), p.toString());
                super.onCreate(event, currentPath);
                if(Files.isDirectory(p)){
                    return;
                }
                String realpath = p.toString();
                if(realpath.endsWith("~")){
                    realpath = realpath.substring(0, realpath.length() - 1);
                }
                File targetFile = new File(HotLoader.target, realpath.replace(HotLoader.resource, ""));
                FileUtil.copy(new File(realpath), targetFile, true);
            }
        },100));
        monitor.setMaxDepth(10);
        monitor.start();
    }


//    @Bean(name = "v20-string-gt")
//    public GroupTemplate groupTemplate() throws IOException {
//        StringTemplateResourceLoader resourceLoader = new StringTemplateResourceLoader();
//        org.beetl.core.Configuration cfg = org.beetl.core.Configuration.defaultConfiguration();
//        cfg.setStatementStart("@");
//        cfg.setStatementEnd(null);
//        cfg.setPlaceholderStart("#");
//        cfg.setPlaceholderEnd("#");
//        GroupTemplate gt = new GroupTemplate(resourceLoader, cfg);
//        gt.registerFunction("mytag", (paras, ctx) -> {
//            Object _ctx = ctx.getGlobal("context");
//            for (Method declaredMethod : _ctx.getClass().getDeclaredMethods()) {
//                if(declaredMethod.getName().contains("mytag")) {
//                    try {
//                        return declaredMethod.invoke(_ctx, paras, ctx);
//                    } catch (IllegalAccessException | InvocationTargetException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }
//            return null;
////            if(_ctx.getClass().getDeclaredMethod("mytag", ))
////            Object userServ = V20.Bean(UserServ.class.getName());
////            List mytags = null;
////            try {
////                Method method = userServ.getClass().getDeclaredMethod("getTags", String.class);
////                mytags = (List) method.invoke(userServ, AuthFilter.uid());
////            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
////                e.printStackTrace();
////            }
////            StringBuilder sb = new StringBuilder();
////            //0必定为string
////            sb.append(paras[0]);
////            sb.append(":");
////            //我的标签必须在这里被包围
////            sb.append("{ $in: [");
////            String left = (String) mytags.stream()
////                .map(e -> (Obj)Json.cast(e, Obj.class))
////                .filter(e -> tags.contains(((Obj)e).s("tag")))
////                .map(e -> "'" + ((Obj)e).s("tag") + "'")
////                .collect(Collectors.joining(","));
////            sb.append(left);
////            sb.append("]");
////            sb.append("}");
////            try {
////                ctx.byteWriter.write(sb.toString().getBytes(StandardCharsets.UTF_8));
////            } catch (IOException e) {
////                throw new RuntimeException(e);
////            }
////            return "";
//        });
//        return gt;
//    }

//    public static boolean isWin(){
//        return isWin;
//    }
//
//    public static boolean isDev(){
//        return dev;
////        String path = HotLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
////        return !path.contains(".jar!");
////        if (env == null) {
////            return false;
////        }
////        String[] actives = env.getActiveProfiles();
////        if (actives == null) {
////            return false;
////        }
////        for (String active : actives) {
////            if(active.equals("myserver")){
////                return true;
////            }
////        }
////        return false;
//    }


    public static void AutoWiredBean(Object instance) {
        Class clz = instance.getClass();
        while(clz != null){
            //生效spring的注入
            for (Field field : clz.getDeclaredFields()) {
                Autowired autowired = field.getAnnotation(Autowired.class);
                Object bean = null;
                if (autowired != null) {
                    field.setAccessible(true);
                    Qualifier qualifier = field.getAnnotation(Qualifier.class);
                    try{
                        if (qualifier != null) {
                            bean = context.getBean(qualifier.value());
                        } else {
                            bean = context.getBean(field.getType());
                        }
                        field.set(instance, bean);
                        continue;
                    } catch (Exception e){
                    }
                }
                V20Auto auto = field.getAnnotation(V20Auto.class);
                if (auto != null) {
                    bean = Bean(field.getType());
                    try {
                        field.setAccessible(true);
                        field.set(instance, bean);
                    } catch (IllegalAccessException e) {
                    }
                    continue;
                }
            }
            clz = clz.getSuperclass();
        }

    }

    /**
     * 只能动态加载V20下的bean
     * @param clz
     * @param <T>
     * @return
     */
    @Deprecated
    public static <T> T Bean(Class<T> clz) {
        Object instance = null;
        try {
            instance = clz.newInstance();
            AutoWiredBean(instance);
            return (T) instance;
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public static Object Bean(String className) {
        ClassLoader loader;
        Class clzz;
        try{
            if(isDev){
                loader = new HotLoader();
                clzz = loader.loadClass(className);
                return Bean(clzz);
            } else {
                loader = NamiSpringController.class.getClassLoader();
                clzz = loader.loadClass(className);
                return Bean(clzz);
            }
        } catch (Exception e){
            return null;
        }
    }

    public static Class Clz(String className){
        Class clzz;
        try{
            ClassLoader loader;
            if(isDev){
                loader = new HotLoader();
                return clzz = loader.loadClass(className);
            } else {
                loader = NamiSpringController.class.getClassLoader();
                return clzz = loader.loadClass(className);
            }
        } catch (Exception e){
            return null;
        }
    }

    @Deprecated
    public static Object Bean(String className, byte[] bs){
        ByteCodeLoader loader = new ByteCodeLoader(className, bs);
        try {
            Class<?> clzz = loader.loadClass(className);
            return Bean(clzz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    public static

}
