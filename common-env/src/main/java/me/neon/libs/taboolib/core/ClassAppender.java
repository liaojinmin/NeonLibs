package me.neon.libs.taboolib.core;

import sun.misc.Unsafe;

import me.neon.libs.taboolib.core.classloader.*;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

/**
 * @author sky
 * @since 2020-04-12 22:39
 */
public class ClassAppender {

    static MethodHandles.Lookup lookup;
    static Unsafe unsafe;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
            Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object lookupBase = unsafe.staticFieldBase(lookupField);
            long lookupOffset = unsafe.staticFieldOffset(lookupField);
            lookup = (MethodHandles.Lookup) unsafe.getObject(lookupBase, lookupOffset);
        } catch (Throwable ignore) {}
    }

    /**
     * 加载一个文件到 ClassLoader
     *
     * @param path       路径
     * @param isIsolated 是否隔离
     */
    public static ClassLoader addPath(Path path, boolean isIsolated) throws Throwable {
        File file = new File(path.toUri().getPath());
        // IsolatedClassLoader
        if (isIsolated) {
            IsolatedClassLoader loader = IsolatedClassLoader.INSTANCE;
            loader.addURL(file.toURI().toURL());
            return loader;
        }

        ClassLoader loader = PrimitiveIO.class.getClassLoader();
        // Application
        if (loader.getClass().getName().equals("jdk.internal.loader.ClassLoaders$AppClassLoader")) {
            addURL(loader, ucp(loader.getClass()), file);
        }
        // Hybrid
        else if (loader.getClass().getName().equals("net.minecraft.launchwrapper.LaunchClassLoader")) {
            MethodHandle methodHandle = lookup.findVirtual(URLClassLoader.class, "addURL", MethodType.methodType(void.class, URL.class));
            methodHandle.invoke(loader, file.toURI().toURL());
        }
        // Bukkit
        else {
            addURL(loader, ucp(loader), file);
        }
        return loader;
    }

    /**
     * 获取 addPath 函数所使用的 ClassLoader（原函数为：judgeAddPathClassLoader）
     */
    public static ClassLoader getClassLoader() {
        return IsolatedClassLoader.INSTANCE;
    }

    /**
     * 判断类是否粗在
     */
    public static boolean isExists(String path) {
        try {
            Class.forName(path, false, getClassLoader());
            //PrimitiveIO.println("查询 %s 存在", path);
            return true;
        } catch (ClassNotFoundException ignored) {
            //PrimitiveIO.println("查询 %s 不存在", path);
            return false;
        }
    }

    private static void addURL(ClassLoader loader, Field ucpField, File file) throws Throwable {
        if (ucpField == null) {
            throw new IllegalStateException("ucp field not found");
        }
        Object ucp = unsafe.getObject(loader, unsafe.objectFieldOffset(ucpField));
        try {
            MethodHandle methodHandle = lookup.findVirtual(ucp.getClass(), "addURL", MethodType.methodType(void.class, URL.class));
            methodHandle.invoke(ucp, file.toURI().toURL());
        } catch (NoSuchMethodError e) {
            throw new IllegalStateException("Unsupported (classloader: " + loader.getClass().getName() + ", ucp: " + ucp.getClass().getName() + ")", e);
        }
    }

    private static Field ucp(ClassLoader loader) {
        try {
            return URLClassLoader.class.getDeclaredField("ucp");
        } catch (NoSuchFieldError | NoSuchFieldException ignored) {
            return ucp(loader.getClass());
        }
    }

    private static Field ucp(Class<?> loader) {
        try {
            return loader.getDeclaredField("ucp");
        } catch (NoSuchFieldError | NoSuchFieldException e2) {
            Class<?> superclass = loader.getSuperclass();
            if (superclass == Object.class) {
                return null;
            }
            return ucp(superclass);
        }
    }

}
