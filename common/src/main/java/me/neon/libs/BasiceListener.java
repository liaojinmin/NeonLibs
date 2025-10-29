package me.neon.libs;

import me.neon.libs.chunk.WorldDataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 * NeonLibs
 * me.neon.libs
 *
 * @author 老廖
 * @since 2024/3/1 12:07
 */
public class BasiceListener implements Listener {

    @EventHandler
    private void onWorldLoad(WorldLoadEvent event) {
        WorldDataManager.INSTANCE.onWorldLoad(event.getWorld());
    }

    @EventHandler
    private void onWorldUnload(WorldUnloadEvent event) {
        WorldDataManager.INSTANCE.onWorldUnload(event.getWorld());
    }

    @EventHandler
    private void onChunkLoad(ChunkLoadEvent event) {
        WorldDataManager.INSTANCE.onChunkLoad(event.getChunk());
    }

    @EventHandler
    private void onChunkUnload(ChunkUnloadEvent event) {
        WorldDataManager.INSTANCE.onChunkUnload(event.getChunk());
    }
}
