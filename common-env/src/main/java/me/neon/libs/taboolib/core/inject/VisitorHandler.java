package me.neon.libs.taboolib.core.inject;

import me.neon.libs.taboolib.ProjectScannerKt;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.tabooproject.reflex.ClassField;
import org.tabooproject.reflex.ClassMethod;
import org.tabooproject.reflex.ReflexClass;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;

/**
 * TabooLib
 * taboolib.common.inject.VisitorHandler
 *
 * @author sky
 * @since 2021/8/14 12:18 上午
 */
@SuppressWarnings("CallToPrintStackTrace")
public class VisitorHandler {

    private static final NavigableMap<Byte, VisitorGroup> propertyMap = Collections.synchronizedNavigableMap(new TreeMap<>());

    /**
     * 注册依赖注入接口
     *
     * @param classVisitor 接口
     */
    public static void register(@NotNull ClassVisitor classVisitor) {
        VisitorGroup injectors = propertyMap.computeIfAbsent(classVisitor.getPriority(), i -> new VisitorGroup(classVisitor.getPriority()));
        injectors.getAll().add(classVisitor);
    }

    /**
     * 对给定类进行依赖注入
     *
     * @param clazz 类
     */
    public static void injectAll(@NotNull Plugin plugin, @NotNull Class<?> clazz) {
        for (Map.Entry<Byte, VisitorGroup> entry : propertyMap.entrySet()) {
            inject(plugin, clazz, entry.getValue());
        }
    }

    /**
     * 对给定类进行依赖注入
     *
     * @param clazz     类
     * @param group     注入组
     */
    private static void inject(@NotNull Plugin plugin, @NotNull Class<?> clazz, @NotNull VisitorGroup group) {
        // 获取实例
        Supplier<?> instance ;
        if (Modifier.isStatic(clazz.getModifiers())) {
            instance = ProjectScannerKt.getInstance(clazz, false);
        } else {
            instance = ProjectScannerKt.getInstance(clazz, true);
        }
        // 获取结构
        ReflexClass rc;
        try {
            rc = ReflexClass.Companion.of(clazz, true);
        } catch (Throwable ex) {
            new ClassVisitException(clazz, ex).printStackTrace();
            return;
        }
        // 依赖注入
        visitStart(plugin, clazz, group, rc, instance);
        visitField(plugin, clazz, group, rc, instance);
        visitMethod(plugin, clazz, group, rc, instance);
        visitEnd(plugin, clazz, group, rc, instance);
    }

    private static void visitStart(@NotNull Plugin plugin, Class<?> clazz, VisitorGroup group, ReflexClass reflexClass, Supplier<?> instance) {
        for (ClassVisitor visitor : group.get()) {
            try {
                visitor.visitStart(plugin, clazz, instance);
            } catch (Throwable ex) {
                new ClassVisitException(clazz, group, ex).printStackTrace();
            }
        }
    }

    private static void visitField(@NotNull Plugin plugin, Class<?> clazz, VisitorGroup group, ReflexClass reflexClass, Supplier<?> instance) {
        for (ClassVisitor visitor : group.get()) {
            for (ClassField field : reflexClass.getStructure().getFields()) {
                try {
                    visitor.visit(plugin, field, clazz, instance);
                } catch (Throwable ex) {
                    new ClassVisitException(clazz, group, field, ex).printStackTrace();
                }
            }
        }
    }

    private static void visitMethod(@NotNull Plugin plugin, Class<?> clazz, VisitorGroup group, ReflexClass reflexClass, Supplier<?> instance) {
        for (ClassVisitor visitor : group.get()) {
            for (ClassMethod method : reflexClass.getStructure().getMethods()) {
                try {
                    visitor.visit(plugin, method, clazz, instance);
                } catch (Throwable ex) {
                    new ClassVisitException(clazz, group, method, ex).printStackTrace();
                }
            }
        }
    }

    private static void visitEnd(@NotNull Plugin plugin, Class<?> clazz, VisitorGroup group, ReflexClass reflexClass, Supplier<?> instance) {
        for (ClassVisitor visitor : group.get()) {
            try {
                visitor.visitEnd(plugin, clazz, instance);
            } catch (Throwable ex) {
                new ClassVisitException(clazz, group, ex).printStackTrace();
            }
        }
    }

}
