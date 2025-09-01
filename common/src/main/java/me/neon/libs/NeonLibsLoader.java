package me.neon.libs;

import me.neon.libs.core.ProjectScannerKt;
import me.neon.libs.core.env.*;
import me.neon.libs.core.inject.*;
import me.neon.libs.event.EventRegisterVisitor;
import me.neon.libs.service.IService;
import me.neon.libs.core.LifeCycle;
import me.neon.libs.core.classloader.IsolatedClassLoader;
import me.neon.libs.util.RunnerDslKt;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * NeonLibs
 * me.neon.libs
 *
 * @author 老廖
 * @since 2024/2/28 20:33
 */
@RuntimeDependencies({
        @RuntimeDependency(
                value = "!org.openjdk.nashorn:nashorn-core:15.4",
                test = "!jdk.nashorn.api.scripting.NashornScriptEngineFactory"
        )

})
public class NeonLibsLoader extends JavaPlugin {

    private static final ConcurrentHashMap<String, LifeCycleLoader> registerPlugin = new ConcurrentHashMap<>();

    private static final Logger logger = Logger.getLogger("NeonLibs");

    private static Plugin instance;

    private final Set<Class<?>> classes;

    public static LifeCycleLoader initializePlugin(Plugin plugin, Class<? extends Plugin> clazz, Runnable disableRun) {
        if (registerPlugin.containsKey(plugin.getName())) {
            unloadPlugin(plugin);
        }
        LifeCycleLoader lifeCycleLoader = new LifeCycleLoader(plugin, clazz, disableRun);
        registerPlugin.put(plugin.getName(), lifeCycleLoader);
        return lifeCycleLoader;
    }

    public static void unloadPlugin(Plugin plugin) {
        LifeCycleLoader lifeCycleLoader = registerPlugin.remove(plugin.getName());
        if (lifeCycleLoader != null) {
            lifeCycleLoader.disable();
        }
    }

    public static LifeCycleLoader getLifeCycleLoader(Plugin plugin) {
        return registerPlugin.get(plugin.getName());
    }

    public static void print(Object message) {
        logger.info(Objects.toString(message));
    }

    public static void info(Object message) {
        logger.info(Objects.toString(message));
    }

    public static void warning(Object message) {
        logger.warning(Objects.toString(message));
    }

    public static NeonLibsLoader getInstance() {
        return (NeonLibsLoader) instance;
    }

    static {
        // 启动 IsolatedClassLoader
        IsolatedClassLoader.init(NeonLibsLoader.class);


    }


    public NeonLibsLoader() {
        instance = this;
        try {
            RuntimeEnv.ENV.inject(instance.getClass());
            RuntimeEnv.ENV.inject(IService.class);
            VisitorHandler.register(this, new EventRegisterVisitor());
            for (LifeCycle lifeCycle : LifeCycle.values()) {
                VisitorHandler.register(this, new AwakeFunctionVisitor(lifeCycle));
            }
            // 对功能模块进行唤醒
            Map<String, Class<?>> map = ProjectScannerKt.getClasses(this.getClass().getProtectionDomain().getCodeSource().getLocation(), this.getClassLoader());
            classes = new HashSet<>(map.size());

            // 只对 taboolib、carrier、Action 目录下的类进行唤醒
            map.forEach((key, value) -> {
                if (key.contains(".taboolib.") || key.contains(".carrier.")) {
                    classes.add(value);
                }
            });
            classes.forEach(it -> {
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
                        VisitorHandler.register(this, (ClassVisitor) instance.get());
                    }
                }
            });
            VisitorHandler.injectAll(this, LifeCycle.NONE, classes);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onLoad() {
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage("正在加载 §3§lNeonLibs  §f...  §8" + Bukkit.getVersion());
        Bukkit.getConsoleSender().sendMessage("");
    }

    @Override
    public void onEnable() {
        VisitorHandler.injectAll(this, LifeCycle.ENABLE, classes);

        Bukkit.getPluginManager().registerEvents(new LifeCycleListener(), this);
        // NBTAPI
        de.tr7zw.changeme.nbtapi.utils.MinecraftVersion.disableBStats();
        de.tr7zw.changeme.nbtapi.utils.MinecraftVersion.disableUpdateCheck();
        de.tr7zw.changeme.nbtapi.utils.MinecraftVersion.replaceLogger(this.getLogger());
        de.tr7zw.changeme.nbtapi.utils.MinecraftVersion.getVersion();

        RunnerDslKt.syncRunner(5, () -> VisitorHandler.injectAll(this, LifeCycle.ACTIVE, classes));
    }

    @Override
    public void onDisable() {
        VisitorHandler.injectAll(this, LifeCycle.DISABLE, classes);
        registerPlugin.values().forEach(LifeCycleLoader::disableCall);
    }
}
