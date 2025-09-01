package me.neon.agent;

import net.minecraft.server.v1_12_R1.PlayerAbilities;

import java.lang.reflect.Field;


/**
 * NeonLibs
 * me.neon.agent
 *
 * @author 老廖
 * @since 2025/8/31 14:37
 */
public class FlyHook {

    public static void onFlyingSet(PlayerAbilities abilities, boolean value) {
        System.out.println("[FlyAgent] abilities.isFlying 被修改 玩家对象=" + abilities + " 新值=" + value);
        new Throwable("调用堆栈").printStackTrace();
    }

    /**
     * 拦截飞行属性赋值
     * @param abilities PlayerAbilities 对象
     * @param newValue  即将写入的值
     * @param fieldName 被修改的字段 (isFlying / canFly)
     */
    public static void onChange(PlayerAbilities abilities, boolean newValue, String fieldName) {
        try {
            System.out.println("[FlyAgent] 尝试修改 " + fieldName + " -> " + newValue);

            // 判断权限逻辑
            boolean allowed = true;
            if ("isFlying".equals(fieldName)) {
                // 举例：完全禁止飞行
                allowed = false;
            } else if ("canFly".equals(fieldName)) {
                // 举例：只允许服务端代码改，不允许反序列化改
                // （你可以通过 Thread.currentThread().getStackTrace() 来判断调用来源）
                allowed = true;
            }

            // 强制写回（覆盖修改）
            Field f = PlayerAbilities.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.setBoolean(abilities, allowed && newValue);

            if (!allowed) {
                System.out.println("[FlyAgent] 已阻止飞行开关，强制设置为 false");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
