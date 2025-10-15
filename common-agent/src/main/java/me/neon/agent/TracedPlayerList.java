package me.neon.agent;

import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.World;

import java.util.ArrayList;

/**
 * NeonLibs
 * me.neon.agent
 *
 * @author 老廖
 * @since 2025/9/15 22:30
 */
public class TracedPlayerList extends ArrayList<EntityHuman> {

    private final World world;

    public TracedPlayerList(World world) {
        super();
        this.world = world;
    }

    @Override
    public boolean add(EntityHuman e) {
        if (!EntityTracerAgent.isBukkitMainThread()) {
            EntityTracerAgent.trace("ADD_ENTITY_HUMAN", e);
        }
        return super.add(e);
    }

    @Override
    public boolean remove(Object o) {
        if (!EntityTracerAgent.isBukkitMainThread()) {
            EntityTracerAgent.trace("REMOVE_ENTITY_HUMAN", o);
        }
        return super.remove(o);
    }

    @Override
    public EntityHuman remove(int index) {
        EntityHuman human = super.remove(index);
        if (!EntityTracerAgent.isBukkitMainThread()) {
            EntityTracerAgent.trace("REMOVE_ENTITY_HUMAN", human);
        }
        return human;
    }




}

