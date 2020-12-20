package org.shoulder.decompile.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReUtil;
import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.util.getopt.OptionsImpl;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 反编译工具
 *
 * @author lym
 */
public class DecompileUtil {

    private static ExecutorService executor = new ThreadPoolExecutor(50, 50,
            2, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy());

    private static AtomicInteger count = new AtomicInteger(0);

    private static List<File> unexceptedFiles = new LinkedList<>();
    private static List<Project> projectList = new LinkedList<>();

    private static int size_128kb = 1280 * 1024;

    private static List<String> largeJars = new LinkedList<>();

    public static void main(String[] args) throws Exception {
        String jarsFileDir = "F:\\codes\\copy\\lib\\xxx";
        String decompileAimPath = "F:\\codes\\source\\xxx";
        String aimGroupId = "com.xxx";
        List<File> wannerJars = findWannerJars(new File(jarsFileDir), aimGroupId);
        // copy
        projectList.sort(Project::compareTo);
        projectList.forEach(System.out::println);
        for (File file : wannerJars) {
            System.out.println(file.getPath());
            if(file.length() > size_128kb){
                largeJars.add(file.getPath());
                continue;
            }
            executor.execute(() -> {
                try {
                    String version = analyzeVersion(new JarFile(file));
                    String aimPath = decompileAimPath + file.getPath().substring(jarsFileDir.length());
                    aimPath = aimPath.replace("-" + version + ".jar", "");
                    doDecompile(file.getPath(), aimPath + "\\src\\main\\java");
                    // 拿出 pom.xml 到特定位置
                    addOtherFile(new JarFile(file), aimPath);
                    // 将 unicode 转中文
                    formatJavaFile(aimPath + "\\src\\main\\java");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        System.err.println("Large Files:");
        largeJars.forEach(System.err::println);

        executor.shutdown();
    }


    // -------------------------- 其他 ----------------------------------

    private static String printJarInfo(JarFile jarFile) throws IOException {
        Enumeration myEnum = jarFile.entries();
        while(myEnum.hasMoreElements()){
            JarEntry entry = (JarEntry)myEnum.nextElement();
            if(entry.getName().endsWith("pom.xml")){
                if(ReUtil.isMatch("META-INF/maven/.*/pom.xml", entry.getName())){
                    Project project = Project.readFromXml(jarFile.getInputStream(entry));
                    projectList.add(project);
                }
            }
        }
        return "";
    }

    private static String analyzeVersion(JarFile jarFile) throws IOException {
        Enumeration myEnum = jarFile.entries();
        while(myEnum.hasMoreElements()){
            JarEntry entry = (JarEntry)myEnum.nextElement();
            if(entry.getName().endsWith("pom.properties")){
                if(ReUtil.isMatch("META-INF/maven/.*/pom.properties", entry.getName())){
                    Properties properties = new Properties();
                    properties.load(jarFile.getInputStream(entry));
                    return (String) properties.get("version");
                }
            }
        }
        return "";
    }


    // -------------------------- 第三步 ： 完善工程 ----------------------------------

    public static void formatJavaFile(String path) throws IOException {
        File dir = new File(path);
        assert dir.isDirectory();
        File[] files = dir.listFiles();
        if(files == null){
            return;
        }
        for (File file : files) {
            if(file.isDirectory()){
                formatJavaFile(file.getPath());
            }else if(file.isFile() && file.getName().endsWith(".java")) {
                //Unicode2CN.toCN(file);
                executor.execute(() -> {
                    try {
                        // 转
                        Unicode2CN.toCN(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }


    private static void addOtherFile(JarFile jarFile, String aimPath) throws IOException {
        Enumeration myEnum = jarFile.entries();
        while(myEnum.hasMoreElements()){
            JarEntry entry = (JarEntry)myEnum.nextElement();
            String entryName = entry.getName();
            if(entryName.endsWith("pom.xml")){
                FileUtil.writeFromStream(jarFile.getInputStream(entry), aimPath + "\\pom.xml");
            }
            if(entryName.contains("META-INF") || entryName.endsWith(".class")){
                continue;
            }
            // 其余文件放到 resource 路径
            // todo 忽略 git.properties ？
            if(!entry.isDirectory()){
                FileUtil.writeFromStream(jarFile.getInputStream(entry), aimPath + "\\src\\main\\resources\\" + entryName);
            }
        }
    }


    // -------------------------- 第二步 ： 反编译 ----------------------------------

    /*public static void decompile(String file){
        DecompileUtil.decompile(new File(file));
    }

    public static void decompile(String file, String aimDir){
        DecompileUtil.decompile(new File(file), aimDir);
    }

    *//**
     * 反编译到 jar 同目录的同名文件夹下
     * @param file 需要反编译的文件或目录
     *//*
    public static void decompile(File file) {
        String path = file.getPath();
        String defaultAimPath = path.substring(0, path.length() - 4) + "-source\\src\\main\\java";
        decompile(file, defaultAimPath);
    }

    public static void decompile(File file, String aimPath) {
        String path = file.getPath();
        if(file.isFile()){
            if(!file.getName().endsWith(".jar") && !file.getName().endsWith(".class")){
                return;
            }
            if(file.length() > size_128kb){
                largeJars.add(path);
                return;
            }
            executor.execute(() -> {
                try {
                    doDecompile(path, aimPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else if(file.isDirectory()){
            File[] subFiles = file.listFiles();
            if(subFiles == null){
                return;
            }
            for (File f : subFiles) {
                decompile(f, aimPath);
            }
        }
    }*/

    /**
     * 调用 cfr
     */
    public static void doDecompile(String sourceJar, String aimPath) {
        Map<String, String> opts = new HashMap<>();
        opts.put("caseinsensitivefs", "true");
        opts.put("outputdir", aimPath);
        OptionsImpl options = new OptionsImpl(opts);
        CfrDriver cfrDriver = new CfrDriver.Builder().withBuiltOptions(options).build();
        cfrDriver.analyse(Collections.singletonList(sourceJar));
    }

    // -------------------------- 第一步 ： 找到想要的 jar ----------------------------------


    public static List<File> findWannerJars(File dir, String packagePath) throws IOException {
        File[] files = dir.listFiles();
        if(files == null){
            return Collections.emptyList();
        }
        List<File> wanners = new LinkedList<>();
        for (File file : files) {
            if(file.getName().endsWith(".jar")){
                JarFile jarFile = new JarFile(file);
                printJarInfo(jarFile);
                if(isWannerJar(jarFile, packagePath)){
                    wanners.add(file);
                }else {
                    unexceptedFiles.add(file);
                }
                jarFile.close();
            } else if(file.isDirectory()){
                wanners.addAll(findWannerJars(file, packagePath));
            }
        }
        return wanners;
    }

    /**
     * web / spring boot jar / 特定包路径
     * @param packagePath
     * @return
     */
    public static boolean isWannerJar(JarFile jarFile, String packagePath) {
        Enumeration myEnum = jarFile.entries();
        while(myEnum.hasMoreElements()){
            JarEntry entry = (JarEntry)myEnum.nextElement();
            if(entry.getName().endsWith("pom.xml")){
                return ReUtil.isMatch("META-INF/maven/" + packagePath + ".*/pom.xml", entry.getName());
            } else if("WEB-INF".equals(entry.getName())){
                return true;
            } else if(entry.getName().startsWith("application.") && (
                    entry.getName().endsWith("yml") ||entry.getName().endsWith("properties") ||entry.getName().endsWith("yaml")
            )){
                return true;
            }
        }
        return false;
    }

}
