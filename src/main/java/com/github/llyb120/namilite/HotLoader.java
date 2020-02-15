package com.github.llyb120.namilite;


import cn.hutool.core.io.FileUtil;
import com.github.llyb120.json.Arr;
import com.github.llyb120.json.Json;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.github.llyb120.json.Json.*;
import static com.github.llyb120.namilite.init.NamiBean.namiConfig;
import static com.github.llyb120.namilite.init.NamiLite.*;


public class HotLoader extends ClassLoader {

    public static String src;
    public static String target;
    public static String resource;
    public static String resourceTarget;

    static {
        src = GenKit.getJavaSRCPath();
        resource = GenKit.getJavaResourcePath();//new File(src + "/main/resources").getAbsolutePath();
//        src = new File(src + "/main/java").getAbsolutePath();
        target = src + "/../../../target/classes";
    }

    private static JavaCompiler javac = ToolProvider.getSystemJavaCompiler(); //new EcjCompiler();
    private static ClassLoader defaultLoader = HotLoader.class.getClassLoader();


    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return findClass(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class clz = findLoadedClass(name);
        if (clz != null) {
            return clz;
        }
        check:{
            for (String hotPackage : namiConfig.hotPackages()) {
                if(name.startsWith(hotPackage)){
                    break check;
                }
            }
            return defaultLoader.loadClass(name);
        }
        try {
            //得到源代码上一次的时间
            File src = toSrcFile(name);
            //得到编译后的代码时间
            File cpd = toClassFile(name);
            if (!cpd.exists() || src.lastModified() > cpd.lastModified()) {
                Future future = compile(src);
                future.get();
            }
            byte[] bs = FileUtil.readBytes(cpd);
            return defineClass(name, bs, 0, bs.length);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultLoader.loadClass(name);
        }
    }


    private File toSrcFile(String name) {
        return new File(src, name.replace(".", "/") + ".java");
    }

    private File toClassFile(String name) {
        return new File(src + "/../../../target/classes", name.replace(".", "/") + ".class");
    }

    private static File toTargetFilePath(File file) {
        String path = file.getParentFile().getAbsolutePath();
        String relative = path.replace(src, "");
        return new File(target, relative + "/" + file.getName().replace(".java", ".class"));
    }

    private static ExecutorService compileThread = Executors.newSingleThreadExecutor();

    public static Future compile(File... files) {
        return compileThread.submit(() -> {
            List targets = Arrays.stream(files)
                    .filter(e -> {
                        //排除掉已经编译的代码
                        File targetFile = toTargetFilePath(e);
                        return !targetFile.exists() || (targetFile.lastModified() < e.lastModified());
                    })
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toList());
            if (targets.isEmpty()) {
                return;
            }
            Arr<?> args = a(
//                    "-noExit",
//                    "-proceedOnError",
                    "-parameters",
                    "-nowarn",
                    "-source",
                    "1.8",
                    "-sourcepath",
                    src,
                    "-d",
                    target
//            files
//            file.getAbsolutePath()
            );
            args.addAll(targets);
            int status = javac.run(
                    null,
                    null,
                    null,
                    args.toArray(new String[0])
            );
        });
    }


    /**
     * 给接口测试用的，不需要改
     *
     * @param file
     * @param outDir
     */
    public static void compile(File file, File outDir) {
        Arr<?> args = aaa(
//                "-noExit",
//                "-proceedOnError",
                "-parameters",
                "-nowarn",
                "-source",
                "1.8",
                isDev ? undefined : "-cp",
                isDev ? undefined : cp,
                "-d",
                outDir.getAbsolutePath(),
                file.getAbsolutePath()
        );
        System.out.println(Json.stringify(args));
        int status = javac.run(
                null,
                null,
                null,
                args.toArray(new String[0])
        );
    }



    public static class GenKit {
        private static String srcPathRelativeToSrc = "/main/java";
        private static String resourcePathRelativeToSrc = "/main/resources";

        public GenKit() {
        }

        public static String getJavaSRCPath() {
            return getPath(srcPathRelativeToSrc);
        }

        public static String getJavaResourcePath() {
            return getPath(resourcePathRelativeToSrc);
        }

        public static void setSrcPathRelativeToSrc(String srcPathRelativeToSrc) {
            GenKit.srcPathRelativeToSrc = srcPathRelativeToSrc;
        }

        public static void setResourcePathRelativeToSrc(String resourcePathRelativeToSrc) {
            GenKit.resourcePathRelativeToSrc = resourcePathRelativeToSrc;
        }

        private static String getPath(String relativeToSrc) {
            String userDir = System.getProperty("user.dir");
            if (userDir == null) {
                throw new NullPointerException("用户目录未找到");
            } else {
                File src = new File(userDir, "src");
                File resSrc = new File(src.toString(), relativeToSrc);
                String srcPath;
                if (resSrc.exists()) {
                    srcPath = resSrc.toString();
                } else {
                    srcPath = src.toString();
                }

                return srcPath;
            }
        }
    }
}
