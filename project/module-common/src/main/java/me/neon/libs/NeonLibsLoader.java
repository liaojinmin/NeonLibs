package me.neon.libs;

import me.neon.libs.api.LifeCycleLoader;
import me.neon.libs.service.IService;
import me.neon.libs.taboolib.cmd.CommandLoader;
import me.neon.libs.taboolib.cmd.SimpleCommandRegister;
import me.neon.libs.taboolib.core.ClassAppender;
import me.neon.libs.taboolib.core.classloader.IsolatedClassLoader;
import me.neon.libs.taboolib.core.inject.VisitorHandler;
import me.neon.libs.taboolib.core.env.RuntimeDependencies;
import me.neon.libs.taboolib.core.env.RuntimeDependency;
import me.neon.libs.taboolib.event.EventBus;
import me.neon.libs.taboolib.lang.Language;
import me.neon.libs.taboolib.ui.ClickListener;
import me.neon.libs.taboolib.chat.ChatCapture;
import me.neon.libs.taboolib.ui.MenuHolder;
import me.neon.libs.utils.LocaleI18n;
import me.neon.libs.taboolib.core.env.RuntimeEnv;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
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
                value = "!javax.mail:mail:1.5.0-b01",
                test = "javax.mail.Version",
                relocate = {"!javax.mail", "javax.mail"},
                transitive = false
        ),
        @RuntimeDependency(
                value = "!javax.activation:activation:1.1.1",
                test = "javax.activation.URLDataSource",
                relocate = {"!javax.activation", "javax.activation"},
                transitive = false
        ),
        @RuntimeDependency(
                value = "!redis.clients:jedis:4.2.2",
                test = "redis.clients.jedis.BuilderFactory",
                relocate = {"!redis.clients", "redis.clients"},
                transitive = false
        ),
        @RuntimeDependency(
                value = "!org.openjdk.nashorn:nashorn-core:15.4",
                test = "!jdk.nashorn.api.scripting.NashornScriptEngineFactory"
        )
})
public class NeonLibsLoader extends JavaPlugin {

    private static final ConcurrentHashMap<Plugin, LifeCycleLoader> registerPlugin = new ConcurrentHashMap<>();

    private static final Logger logger = Logger.getLogger("NeonLibs");

    public static Plugin instance;

    public static void registerLanguage(Plugin plugin, File pluginFile) {
        Language.INSTANCE.onLoader(plugin, pluginFile);
    }

    public static LifeCycleLoader initializePlugin(Plugin plugin, Class<? extends Plugin> clazz, Runnable disableRun) {
        LifeCycleLoader lifeCycleLoader = new LifeCycleLoader(plugin, clazz, disableRun);
        registerPlugin.put(plugin, lifeCycleLoader);
        return lifeCycleLoader;
    }

    public static void unloadPlugin(Plugin plugin, boolean call) {
        LifeCycleLoader lifeCycleLoader = registerPlugin.remove(plugin);
        if (lifeCycleLoader != null) {
            lifeCycleLoader.disable(call);
        }
    }

    public static void print(Object message) {
        logger.info(Objects.toString(message));
    }

    public static void warning(Object message) {
        logger.warning(Objects.toString(message));
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
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onLoad() {
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage("正在加载 §3§lNeonLibs  §f...  §8" + Bukkit.getVersion());
        Bukkit.getConsoleSender().sendMessage("");
        //注册注入器
        VisitorHandler.register(new SimpleCommandRegister());
        VisitorHandler.register(new EventBus());
    }

    @Override
    public void onEnable() {
        // i18n
        LocaleI18n.INSTANCE.init$module_common();

        print("正在加载自有语言文件...");
        registerLanguage(this, this.getFile());

        print("正在注册事件监听器...");
        Bukkit.getPluginManager().registerEvents(new ClickListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChatCapture(), this);
    }

    @Override
    public void onDisable()  {
        //关闭已打开的界面
        Bukkit.getOnlinePlayers().forEach(it -> {
            if (MenuHolder.Companion.fromInventory(it.getOpenInventory().getTopInventory()) != null) {
                it.closeInventory();
            }
        });
        // command
        CommandLoader.INSTANCE.unregisterCommands();

        registerPlugin.values().forEach(LifeCycleLoader::disable);
    }
}
