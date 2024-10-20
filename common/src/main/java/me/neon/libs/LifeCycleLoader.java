package me.neon.libs;

import kotlin.collections.MapsKt;
import me.neon.libs.core.LifeCycle;
import me.neon.libs.core.PrimitiveIO;
import me.neon.libs.core.ProjectScannerKt;
import me.neon.libs.core.env.*;
import me.neon.libs.core.inject.ClassVisitor;
import me.neon.libs.core.inject.Instance;
import me.neon.libs.core.inject.Visitor;
import me.neon.libs.core.inject.VisitorHandler;
import me.neon.libs.util.RunnerDslKt;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.Supplier;


/**
 * NeonLibs
 * me.neon.libs.api
 *
 * @author 老廖
 * @since 2024/2/29 20:35
 */
public class LifeCycleLoader {

    private final Plugin plugin;

    private final Map<String, Class<?>> runningClassMapInJar;

    private final Set<Class<?>> classes;

    private final Runnable disableRun;

    private LifeCycle lifeCycle = LifeCycle.NONE;

    LifeCycleLoader(Plugin plugin, Class<? extends Plugin> mainClazz, Runnable disableRun) {
        this.plugin = plugin;
        this.disableRun = disableRun;
        runningClassMapInJar = ProjectScannerKt.getClasses(mainClazz.getProtectionDomain().getCodeSource().getLocation(), mainClazz.getClassLoader());
        classes = new HashSet<>(getRunningExactClassMap().values());
        classes.forEach(it -> {
            // 加载运行环境
            if (it.isAnnotationPresent(RuntimeDependencies.class)
                    || it.isAnnotationPresent(RuntimeDependency.class)
                    || it.isAnnotationPresent(RuntimeResources.class)
                    || it.isAnnotationPresent(RuntimeResource.class)
            ) {
                try {
                    PrimitiveIO.println("正在注入依赖加载 -> "+it);
                    RuntimeEnv.ENV.inject(it);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        });
        classes.forEach(it -> {
            // 注册自唤醒
            if (it.isAnnotationPresent(Visitor.class) && ClassVisitor.class.isAssignableFrom(it)) {
                Supplier<?> instance;
                if (!it.isAnnotationPresent(Instance.class) || Plugin.class.isAssignableFrom(it)) {
                    instance = ProjectScannerKt.getInstance(it, false);
                } else {
                    instance = ProjectScannerKt.getInstance(it, true);
                }
                if (instance != null) {
                    // 依赖注入接口
                    PrimitiveIO.println("正在注册注入器 -> "+it);
                    VisitorHandler.register(plugin, (ClassVisitor) instance.get());
                }
            }
        });
        VisitorHandler.injectAll(plugin, lifeCycle, classes);
    }

    public void enable() {
        lifeCycle = LifeCycle.ENABLE;
        VisitorHandler.injectAll(plugin, lifeCycle, classes);

        // 下一个同步 TICK 可视为已经过启动过程
        RunnerDslKt.syncRunner(5, this::active);
    }

    void active() {
        lifeCycle = LifeCycle.ACTIVE;
        VisitorHandler.injectAll(plugin, lifeCycle, classes);
    }

    void disable() {
        lifeCycle = LifeCycle.DISABLE;
        VisitorHandler.injectAll(plugin, lifeCycle, classes);

        // 移除语言
        NeonLibsLoader.print("["+ plugin.getName() +"] 注销语言功能...");
       // Language.INSTANCE.onDisable(plugin);

        // 通知注入器移除插件
        VisitorHandler.uninjectAll(plugin);
        runningClassMapInJar.clear();
    }

    void disableCall() {
        disable();
        NeonLibsLoader.print("["+ plugin.getName() +"] 启动关闭回调...");
        disableRun.run();
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public LifeCycle getLifeCycle() {
        return lifeCycle;
    }

    public Map<String, Class<?>> getRunningClassMapWithoutLibrary() {
        return MapsKt.filterKeys(runningClassMapInJar, (it) -> !it.contains(".library.") && !it.contains(".libs."));
    }

    private Map<String, Class<?>> getRunningExactClassMap() {
        Map<String, Class<?>> map = new LinkedHashMap<>();
        for (Map.Entry<String, Class<?>> entry : runningClassMapInJar.entrySet()) {
            String className = entry.getKey();
            Class<?> clazz = entry.getValue();
            // 排除（排除匿名类、内部类）
            if (!isAnonymousClass(className) && !isInnerClass(className)) {
                map.put(className, clazz);
            }
        }
        return map;
    }

    private boolean isAnonymousClass(String className) {
        return className.contains("$");
    }

    private boolean isInnerClass(String className) {
        // 获取类名的最后一个 . 的索引
        int lastIndex = className.lastIndexOf(".");
        // 截取最后一个 . 之后的字符串，如果包含 $ 则为内部类
        return lastIndex != -1 && className.substring(lastIndex + 1).contains("$");
    }


}
