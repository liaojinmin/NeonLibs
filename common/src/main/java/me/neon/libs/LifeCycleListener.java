package me.neon.libs;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

/**
 * NeonLibs
 * me.neon.libs
 *
 * @author 老廖
 * @since 2024/3/1 12:07
 */
public class LifeCycleListener implements Listener {

    @EventHandler
    public void enable(PluginEnableEvent event) {
        LifeCycleLoader lifeCycleLoader = NeonLibsLoader.getLifeCycleLoader(event.getPlugin());
        if (lifeCycleLoader != null) {
            lifeCycleLoader.enable();
        }
    }

    @EventHandler
    public void disable(PluginDisableEvent event) {
        NeonLibsLoader.unloadPlugin(event.getPlugin());
    }
}
