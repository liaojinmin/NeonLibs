package me.neon.libs.core.classloader;

import me.neon.libs.core.env.RuntimeEnv;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

public class IsolatedClassLoader extends URLClassLoader {

    /**
     * 当前项目正在使用的加载器 TODO("未深入使用...")
     * 由 "插件主类" 初始化
     */
    public static IsolatedClassLoader INSTANCE = null;

    private final Set<String> excludedClasses = new HashSet<>();
    private final Set<String> excludedPackages = new HashSet<>();

    public static void init(Class<?> clazz) {
        if (INSTANCE != null) {
            throw new RuntimeException("重复的初始化...");
        }
        // 初始化隔离类加载器
        INSTANCE = new IsolatedClassLoader(clazz);
        // 加载依赖库
        try {
            Class<?> mainClass = RuntimeEnv.ENV.getClass();
            Method method = mainClass.getDeclaredMethod("init");
            method.setAccessible(true);
            method.invoke(null);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    IsolatedClassLoader(Class<?> clazz) {
        this(new URL[]{clazz.getProtectionDomain().getCodeSource().getLocation()}, clazz.getClassLoader());
        excludedClasses.add(clazz.getName());
    }

    IsolatedClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);

        // 默认排除类
        excludedPackages.add("java.");
        // JavaPlugin 直接访问
        excludedClasses.add("classloader.me.neon.libs.core.IsolatedClassLoader");
        // 储存数据
        excludedClasses.add("me.neon.libs.core.ClassAppender");
        excludedClasses.add("me.neon.libs.NeonLibsLoader");

        // Load excluded classes and packages by SPI
        ServiceLoader<IsolatedClassLoaderConfig> serviceLoader = ServiceLoader.load(IsolatedClassLoaderConfig.class, parent);

        for (IsolatedClassLoaderConfig config : serviceLoader) {
            Set<String> configExcludedClasses = config.excludedClasses();
            if (configExcludedClasses != null && !configExcludedClasses.isEmpty()) {
                excludedClasses.addAll(configExcludedClasses);
            }
            Set<String> configExcludedPackages = config.excludedPackages();
            if (configExcludedPackages != null && !configExcludedPackages.isEmpty()) {
                excludedPackages.addAll(configExcludedPackages);
            }
        }
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return loadClass(name, resolve, true);
    }

    private Class<?> loadClass(String name, boolean resolve, boolean checkParents) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> findClass = findLoadedClass(name);
            // Check isolated classes and libraries before parent to:
            //   - prevent accessing classes of other plugins
            //   - prevent the usage of old patch classes (which stay in memory after reloading)
            if (findClass == null && !excludedClasses.contains(name)) {
                boolean flag = true;
                for (String excludedPackage : excludedPackages) {
                    if (name.startsWith(excludedPackage)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    findClass = findClassOrNull(name);
                }
            }
            if (findClass == null && checkParents) {
                findClass = loadClassFromParentOrNull(name);
            }
            if (findClass == null) {
                throw new ClassNotFoundException(name);
            }
            if (resolve) {
                resolveClass(findClass);
            }
            return findClass;
        }
    }

    private Class<?> findClassOrNull(String name) {
        try {
            return findClass(name);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    private Class<?> loadClassFromParentOrNull(String name) {
        try {
            return getParent().loadClass(name);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    public void addExcludedClass(String name) {
        excludedClasses.add(name);
    }

    public void addExcludedClasses(Collection<String> names) {
        excludedClasses.addAll(names);
    }

    public void addExcludedPackage(String name) {
        excludedPackages.add(name);
    }

    public void addExcludedPackages(Collection<String> names) {
        excludedPackages.addAll(names);
    }
}

