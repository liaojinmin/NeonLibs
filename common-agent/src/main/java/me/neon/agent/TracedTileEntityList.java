package me.neon.agent;
import net.minecraft.server.v1_12_R1.TileEntity;
import net.minecraft.server.v1_12_R1.World;

import java.util.ArrayList;

/**
 * NeonLibs
 * me.neon.agent
 *
 * @author 老廖
 * @since 2025/9/15 22:30
 */
public class TracedTileEntityList extends ArrayList<TileEntity> {

    private final World world;

    public TracedTileEntityList(World world) {
        super();
        this.world = world;
    }

    @Override
    public boolean add(TileEntity e) {
        if (!EntityTracerAgent.isBukkitMainThread()) {
            EntityTracerAgent.trace("ADD_TILE_ENTITY", e);
        }
        return super.add(e);
    }

    @Override
    public boolean remove(Object o) {
        if (!EntityTracerAgent.isBukkitMainThread()) {
            EntityTracerAgent.trace("REMOVE_TILE_ENTITY", o);
        }
        return super.remove(o);
    }

    @Override
    public TileEntity remove(int index) {
        TileEntity human = super.remove(index);
        if (!EntityTracerAgent.isBukkitMainThread()) {
            EntityTracerAgent.trace("REMOVE_TILE_ENTITY", human);
        }
        return human;
    }




}

