package me.neon.libs.api;

import me.neon.libs.taboolib.core.PrimitiveIO;
import me.neon.libs.taboolib.core.inject.ClassVisitor;
import me.neon.libs.taboolib.core.inject.Inject;
import me.neon.libs.taboolib.event.Event;
import me.neon.libs.taboolib.ProjectScannerKt;
import me.neon.libs.taboolib.cmd.CommandLoader;
import me.neon.libs.taboolib.core.env.*;
import me.neon.libs.taboolib.core.inject.Awake;
import me.neon.libs.taboolib.core.inject.VisitorHandler;
import me.neon.libs.taboolib.lang.Language;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;


/**
 * NeonLibs
 * me.neon.libs.api
 *
 * @author 老廖
 * @since 2024/2/29 20:35
 */
public class LifeCycleLoader {

    public final Plugin plugin;

    public final Class<? extends Plugin> mainClazz;

    private final Map<String, Class<?>> runningClassMapInJar;

    private final Runnable disableRun;

    public LifeCycleLoader(Plugin plugin, Class<? extends Plugin> mainClazz, Runnable disableRun) {
        this.plugin = plugin;
        this.mainClazz = mainClazz;
        this.disableRun = disableRun;
        runningClassMapInJar = ProjectScannerKt.getClasses(mainClazz.getProtectionDomain().getCodeSource().getLocation(), mainClazz.getClassLoader());
        getRunningExactClassMap().values().forEach(it -> {
            // 注册自唤醒
            if (it.isAnnotationPresent(Awake.class)) {
                Supplier<?> instance = ProjectScannerKt.getInstance(it, true);
                if (instance != null) {
                    // 依赖注入接口
                    if (ClassVisitor.class.isAssignableFrom(it)) {
                        VisitorHandler.register((ClassVisitor) instance.get());
                    }
                }
            }

            // 注入启动
            if (it.isAnnotationPresent(Inject.class)) {
                VisitorHandler.injectAll(plugin, it);
                PrimitiveIO.println("正在注入加载 -> "+it);
            }

            // 依赖环境
            if (it.isAnnotationPresent(RuntimeDependencies.class)
                    || it.isAnnotationPresent(RuntimeDependency.class)
                    || it.isAnnotationPresent(RuntimeResources.class)
                    || it.isAnnotationPresent(RuntimeResource.class)
            ) {
                try {
                    RuntimeEnv.ENV.inject(it);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void disable(Boolean callRun) {
        // 移除语言
        Language.INSTANCE.onDisable(plugin);
        // 取消事件
        Event.INSTANCE.onDisable(plugin);
        // 卸载指令
        CommandLoader.INSTANCE.unregisterCommand(plugin);
        runningClassMapInJar.clear();
        if (callRun) {
            disableRun.run();
        }
    }

    public void disable() {
        disable(true);
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
