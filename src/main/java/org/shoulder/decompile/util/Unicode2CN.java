package org.shoulder.decompile.util;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Unicode2CN {

    public static void main(String[] args) throws IOException {
        // 具体文件
        toCN(new File(""));
    }

    /**
     * @param sourceFile 具体文件
     */
    public static void toCN(File sourceFile) throws IOException {
        // 转
        File aimFile = new File(sourceFile.getPath() + "x");
        toCN(sourceFile, aimFile);
        // 写入源文件
        FileUtil.move(aimFile, sourceFile, true);

    }

    /**
     * 每个文件都比较小，直接全读到内存里
     * @param file
     * @param aim
     * @throws IOException
     */
    public static void toCN(File file, File aim) throws IOException {
        List<String> allLines = new ArrayList<>(1024);
        FileUtil.readLines(file, StandardCharsets.UTF_8, allLines);
        for (int i = 0; i < allLines.size(); i++) {
            allLines.set(i, ascii2Native(allLines.get(i)));
        }
        FileUtil.writeLines(allLines, aim, StandardCharsets.UTF_8);

    }

    public static String ascii2Native(String str) {
        StringBuilder sb = new StringBuilder();
        int begin = 0;
        int index = str.indexOf("\\u");
        while (index != -1) {
            sb.append(str, begin, index);
            sb.append(ascii2Char(str.substring(index, index + 6)));
            begin = index + 6;
            index = str.indexOf("\\u", begin);
        }
        sb.append(str.substring(begin));
        return sb.toString();
    }

    private static char ascii2Char(String str) {
        if (str.length() != 6) {
            throw new IllegalArgumentException("长度不足6位");
        }
        if (!"\\u".equals(str.substring(0, 2))) {
            throw new IllegalArgumentException("字符必须以 \"\\u\"开头.");
        }
        String tmp = str.substring(2, 4);
        int code = Integer.parseInt(tmp, 16) << 8;
        tmp = str.substring(4, 6);
        code += Integer.parseInt(tmp, 16);
        return (char) code;
    }

}