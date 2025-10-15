package me.neon.agent;

import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.World;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 * NeonLibs
 * me.neon.agent
 *
 * @author 老廖
 * @since 2025/9/15 22:30
 */
public class TracedEntityList extends ArrayList<Entity> {

    private final World world;
    private final Field guardField;

    public TracedEntityList(World world) {
        super();
        this.world = world;
        Field f = null;
        try {
            f = world.getClass().getDeclaredField("guardEntityList");
            f.setAccessible(true);
        } catch (NoSuchFieldException ignored) {}
        this.guardField = f;
    }

    @Override
    public boolean add(Entity e) {
        if (!EntityTracerAgent.isBukkitMainThread()) {
            EntityTracerAgent.trace("ADD_ENTITY", e);
        }
        guard();
        //EntityTracerAgent.trace("ADD_ENTITY", e);
        return super.add(e);
    }

    @Override
    public boolean remove(Object o) {
        if (!EntityTracerAgent.isBukkitMainThread()) {
            EntityTracerAgent.trace("REMOVE_ENTITY", o);
        }
        guard();
       // EntityTracerAgent.trace("REMOVE_ENTITY", o);
        return super.remove(o);
    }

    @Override
    public Entity remove(int index) {
        try {
            guard(); // 先 guard
            return super.remove(index);
        } catch (ConcurrentModificationException e) {
            EntityTracerAgent.trace("REMOVE_ENTITY_AT", e);
            throw e; // 可选：是否重新抛出
        }
    }

    private void guard() {
        try {
            if (guardField != null && guardField.getBoolean(world)) {
                throw new ConcurrentModificationException(
                        "TracedEntityList guard triggered!"
                );
            }
        } catch (IllegalAccessException e) {
            // 反射异常这里忽略或打印，但不要吞掉 guard 的逻辑
            e.printStackTrace();
        }
    }


}

