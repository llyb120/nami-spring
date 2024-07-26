package com.github.llyb120.namilite.hotswap;


import cn.hutool.core.io.FileUtil;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.github.llyb120.namilite.init.NamiBean.namiProperties;
import static com.github.llyb120.namilite.init.NamiLite.*;


public class NamiHotLoader extends ClassLoader {

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

//    private static JavaCompiler javac = ToolProvider.getSystemJavaCompiler(); //new EcjCompiler();
    private static JavaCompiler javac;// = new EclipseCompiler();
    private static ClassLoader defaultLoader = NamiHotLoader.class.getClassLoader();


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
        if (!isHotClass(name)) {
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

    public boolean isHotClass(String clzName){
        return false;
//        for (String hotPackage : namiConfig.getFullHotPackages()) {
//            if(clzName.startsWith(hotPackage)) {
//                return true;
//            }
//        }
//        return false;
    }

    public boolean isHotFile(File file){
        return file.exists() && isHotClass(toClassName(file));
    }

    public static String toClassName(File file){
        String name = file.getAbsolutePath().replace(src, "")
                .replaceAll("/|\\\\", ".")
                .replace(".java", "");
            if(name.startsWith(".")){
                name = name.substring(1);
            }
            return name;
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

    /**
     *
     * @param files
     * @return 编译失败的文件
     */
    public static List<File> compileSync(File... files){
        if (javac == null) {
            if(namiProperties.getCompiler().equals("ecj")){
                javac = new EclipseCompiler();
            } else if(namiProperties.getCompiler().equals("javac")){
                javac = ToolProvider.getSystemJavaCompiler();
            } else {
                throw new RuntimeException("error nami compiler");
            }
        }
        List<File> targets = Arrays.stream(files)
            .filter(e -> {
                if(true){
                    return true;
                }
                //排除掉已经编译的代码
                File targetFile = toTargetFilePath(e);
                return !targetFile.exists() || (targetFile.lastModified() < e.lastModified());
            })
            .collect(Collectors.toList());
        if (targets.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> args = new ArrayList<>();
        if(namiProperties.getCompiler().equals("ecj")){
            args.add("-noExit");
            args.add("-proceedOnError");
        }
        args.add("-parameters");
        args.add("-nowarn");
        args.add("-source");
        args.add("1.8");
        if(namiProperties.isUseLombok() && namiProperties.getCompiler().equals("javac") ){
            args.add("-processor");
            args.add("lombok.launch.AnnotationProcessorHider$AnnotationProcessor");
        }
        args.add("-sourcepath");
        args.add(src);
        args.add("-d");
        args.add(target);
//        Arr<?> args = a(
//                $expand,
//                namiProperties.getCompiler().equals("ecj") ? a(
//                    "-noExit",
//                    "-proceedOnError"
//                ) : a(),
//            "-parameters",
//            "-nowarn",
//            "-source",
//            "1.8",
//            $expand,
//            namiProperties.isUseLombok() && namiProperties.getCompiler().equals("javac") ? a(
//                "-processor",
//                "lombok.launch.AnnotationProcessorHider$AnnotationProcessor"
//            ) : a(),
//            "-sourcepath",
//            src,
//            "-d",
//            target
////            files
////            file.getAbsolutePath()
//        );
        args.addAll(
            (List) targets.stream().map(e -> e.getAbsolutePath()).distinct().collect(Collectors.toList())
        );
        int status = javac.run(
            null,
            null,
            new ByteArrayOutputStream(),
            args.toArray(new String[0])
        );

        return status == 0 ? new ArrayList<>() : targets;
    }

    @Deprecated
    public static void initCompiler(){
        File file = null;
        try {
            file = File.createTempFile("test", ".java");
            FileUtil.writeString(String.format("public class %s{public static void main(String[] args){} }", file.getName().replace(".java", "")), file, StandardCharsets.UTF_8);
            List<String> args = Arrays.asList(
                "-noExit",
                "-proceedOnError",
                "-parameters",
                "-nowarn",
                "-source",
                "1.8",
                "-sourcepath",
                src,
                "-d",
                target,
                file.getAbsolutePath()
            );
            int status = javac.run(
                null,
                null,
                null,
                args.toArray(new String[0]));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (file != null) {
                file.delete();
            }
        }
    }

    public static Future compile(File... files) {
        return compileThread.submit(() -> {
            compileSync(files);
        });
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
