package me.neon.agent;

import java.lang.reflect.Field;

/**
 * NeonLibs
 * me.neon.agent
 *
 * @author 老廖
 * @since 2025/9/15 22:39
 */
public class Utils {

    public static Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                Field f = current.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name + " not found in " + clazz);
    }

}

