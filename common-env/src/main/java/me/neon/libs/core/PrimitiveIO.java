package me.neon.libs.core;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * TabooLib
 * taboolib.common.env.IO
 *
 * @author 坏黑
 * @since 2023/3/31 14:59
 */
@SuppressWarnings("CallToPrintStackTrace")
public class PrimitiveIO {

    /**
     * 中央仓库
     */
    public static final String REPO_CENTRAL = "https://maven.aliyun.com/repository/central";

    private static final String runningFileName = "NeonLibs";

    private static final File libsFile = new File(System.getProperty("user.dir") + File.separator + "plugins" +File.separator + "NeonLibs", "libraries");

    private static final File assetsFile = new File(System.getProperty("user.dir") + File.separator + "plugins" +File.separator + "NeonLibs", "assets");

    /**
     * 是否再用 Logger 输出
     **/
    private static boolean useJavaLogger = false;


    static {
        // 检查 Paper 核心控制台拦截工具
        try {
            Class.forName("io.papermc.paper.logging.SysoutCatcher");
            useJavaLogger = true;
        } catch (ClassNotFoundException ignored) {
        }
    }



    /**
     * 控制台输出
     */
    public static void println(Object message, Object... args) {
        if (useJavaLogger) {
            Logger.getLogger(runningFileName).info(String.format(Objects.toString(message), args));
        } else {
            System.out.printf("[" +runningFileName+ "] "+ message + "%n", args);
        }
    }

    /**
     * 控制台输出
     */
    public static void error(Object message, Object... args) {
        if (useJavaLogger) {
            Logger.getLogger(runningFileName).severe(String.format(Objects.toString(message), args));
        } else {
            System.err.printf("[" +runningFileName+ "] "+ message + "%n", args);
        }
    }


    /**
     * 获取文件保存路径
     */
    public static File getLibraryFile() {
        if (!libsFile.exists()) {
            libsFile.mkdirs();
        }
        return libsFile;
    }

    /**
     * 获取资源文件目录
     */
    public static File getAssetsFile() {
        if (!assetsFile.exists()) {
            assetsFile.mkdirs();
        }
        return assetsFile;
    }

    /**
     * 验证文件完整性
     *
     * @param file     文件
     * @param hashFile 哈希文件
     */
    public static boolean validation(File file, File hashFile) {
        return file.exists() && hashFile.exists() && PrimitiveIO.readFile(hashFile).startsWith(PrimitiveIO.getHash(file));
    }

    /**
     * 检查当前 Kotlin 环境是否有效
     */
    public static boolean isKotlinEnvironment() {
        try {
            Class.forName("kotlin.Lazy", false, ClassAppender.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    /**
     * 获取文件哈希，使用 sha-1 算法
     */
    @NotNull
    public static String getHash(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("sha-1");
            try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                byte[] buffer = new byte[1024];
                int total;
                while ((total = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, total);
                }
            }
            StringBuilder result = new StringBuilder();
            for (byte b : digest.digest()) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (IOException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return "null (" + UUID.randomUUID() + ")";
    }

    /**
     * 获取字符串哈希
     */
    public static String getHash(String data) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("sha-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        digest.update(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : digest.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * 读取文件内容
     */
    @NotNull
    public static String readFile(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return readFully(fileInputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "null (" + UUID.randomUUID() + ")";
    }

    /**
     * 从 InputStream 读取全部内容
     *
     * @param inputStream 输入流
     * @param charset     编码
     */
    @NotNull
    public static String readFully(InputStream inputStream, Charset charset) throws IOException {
        return new String(readFully(inputStream), charset);
    }

    /**
     * 从 InputStream 读取全部内容
     *
     * @param inputStream 输入流
     */
    public static byte[] readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            stream.write(buf, 0, len);
        }
        return stream.toByteArray();
    }

    /**
     * 通过 FileChannel 复制文件
     */
    @NotNull
    public static File copyFile(File from, File to) {
        try (FileInputStream fileIn = new FileInputStream(from); FileOutputStream fileOut = new FileOutputStream(to); FileChannel channelIn = fileIn.getChannel(); FileChannel channelOut = fileOut.getChannel()) {
            channelIn.transferTo(0, channelIn.size(), channelOut);
        } catch (IOException t) {
            t.printStackTrace();
        }
        return to;
    }

    /**
     * 下载文件
     *
     * @param url 地址
     * @param out 目标文件
     */
    @SuppressWarnings("StatementWithEmptyBody")
    public static void downloadFile(URL url, File out) throws IOException {
        out.getParentFile().mkdirs();
        InputStream ins = url.openStream();
        OutputStream outs = Files.newOutputStream(out.toPath());
        byte[] buffer = new byte[4096];
        for (int len; (len = ins.read(buffer)) > 0; outs.write(buffer, 0, len))
            ;
        outs.close();
        ins.close();
    }

    public static String getRunningFileName() {
        return runningFileName;
    }
}
