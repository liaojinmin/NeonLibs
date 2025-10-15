package me.neon.libs.core.inject;

import me.neon.libs.core.LifeCycle;
import me.neon.libs.core.ProjectScannerKt;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tabooproject.reflex.ClassField;
import org.tabooproject.reflex.ClassMethod;
import org.tabooproject.reflex.ReflexClass;

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

   // private static final NavigableMap<Byte, VisitorGroup> propertyMap = Collections.synchronizedNavigableMap(new TreeMap<>());

    private static final NavigableMap<String, NavigableMap<Byte, VisitorGroup>> propertyMap = Collections.synchronizedNavigableMap(new TreeMap<>());

    /**
     * 注册依赖注入接口
     *
     * @param classVisitor 接口
     */
    public static void register(@NotNull Plugin plugin, @NotNull ClassVisitor classVisitor) {
        NavigableMap<Byte, VisitorGroup> map = propertyMap.computeIfAbsent(plugin.getName(), i -> Collections.synchronizedNavigableMap(new TreeMap<>()));
        VisitorGroup injectors = map.computeIfAbsent(classVisitor.getPriority(), i -> new VisitorGroup(classVisitor.getPriority()));
        injectors.getAll().add(classVisitor);
    }


    /**
     * 根据生命周期对所有类进行依赖注入
     *
     * @param lifeCycle 生命周期
     */
    public static void injectAll(@NotNull Plugin plugin, @NotNull LifeCycle lifeCycle, @NotNull Set<Class<?>> classes) {
        //System.out.println("当前周期 "+lifeCycle +" 类数量 "+classes.size());
        for (Map.Entry<String, NavigableMap<Byte, VisitorGroup>> entry : propertyMap.entrySet()) {
            for (Map.Entry<Byte, VisitorGroup> entry2 : entry.getValue().entrySet()) {
                for (Class<?> clazz : classes) {
                   // if (plugin.getName().equalsIgnoreCase("NeonModel")) {
                       // System.out.println("clazz: "+clazz);
                //    }
                    inject(plugin, clazz, entry2.getValue(), lifeCycle);
                }
            }
        }
    }

    /**
     * 对给顶的插件取消依赖注入
     */
    public static void uninjectAll(@NotNull Plugin plugin) {
        for (Map.Entry<String, NavigableMap<Byte, VisitorGroup>> entry : propertyMap.entrySet()) {
            for (Map.Entry<Byte, VisitorGroup> entry2 : entry.getValue().entrySet()) {
                entry2.getValue().getAll().forEach(it -> {
                    try {
                        it.visitUnload(plugin);
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                });
            }
        }
        propertyMap.remove(plugin.getName());
    }

    /**
     * 对给定类进行依赖注入
     *
     * @param clazz     类
     * @param group     注入组
     */
    private static void inject(@NotNull Plugin plugin, @NotNull Class<?> clazz, @NotNull VisitorGroup group,  @Nullable LifeCycle lifeCycle) {
        // 获取实例
        Supplier<?> instance;
        if (!clazz.isAnnotationPresent(Instance.class) || Plugin.class.isAssignableFrom(clazz) ) {
            instance = ProjectScannerKt.getInstance(clazz, false);
        } else {
            instance = ProjectScannerKt.getInstance(clazz, true);
        }
        // 获取结构
        ReflexClass rc;
        try {
            rc = ReflexClass.Companion.of(clazz, false);
        } catch (Throwable ex) {
            new ClassVisitException(clazz, ex).printStackTrace();
            return;
        }
        // 依赖注入

        visitStart(plugin, clazz, group, lifeCycle, rc, instance);
        visitField(plugin, clazz, group, lifeCycle, rc, instance);
        visitMethod(plugin, clazz, group, lifeCycle, rc, instance);
        visitEnd(plugin, clazz, group, lifeCycle, rc, instance);
    }

    private static void visitStart(@NotNull Plugin plugin, Class<?> clazz, VisitorGroup group, LifeCycle lifeCycle, ReflexClass reflexClass, Supplier<?> instance) {
        for (ClassVisitor visitor : group.get(lifeCycle)) {
            try {
                visitor.visitStart(plugin, clazz, instance);
                visitor.visitStart(clazz, instance);
            } catch (Throwable ex) {
                new ClassVisitException(clazz, group, ex).printStackTrace();
            }
        }
    }

    private static void visitField(@NotNull Plugin plugin, Class<?> clazz, VisitorGroup group, LifeCycle lifeCycle, ReflexClass reflexClass, Supplier<?> instance) {
        for (ClassVisitor visitor : group.get(lifeCycle)) {
            for (ClassField field : reflexClass.getStructure().getFields()) {
                try {
                    visitor.visitField(plugin, field, clazz, instance);
                    visitor.visitField(field, clazz, instance);
                } catch (Throwable ex) {
                    new ClassVisitException(clazz, group, field, ex).printStackTrace();
                }
            }
        }
    }

    private static void visitMethod(@NotNull Plugin plugin, Class<?> clazz, VisitorGroup group, LifeCycle lifeCycle, ReflexClass reflexClass, Supplier<?> instance) {
        for (ClassVisitor visitor : group.get(lifeCycle)) {
            for (ClassMethod method : reflexClass.getStructure().getMethods()) {
                try {
                    visitor.visitMethod(plugin, method, clazz, instance);
                    visitor.visitMethod(method, clazz, instance);
                } catch (Throwable ex) {
                    new ClassVisitException(clazz, group, method, ex).printStackTrace();
                }
            }
        }
    }

    private static void visitEnd(@NotNull Plugin plugin, Class<?> clazz, VisitorGroup group, LifeCycle lifeCycle, ReflexClass reflexClass, Supplier<?> instance) {
        for (ClassVisitor visitor : group.get(lifeCycle)) {
            try {
                visitor.visitEnd(plugin, clazz, instance);
                visitor.visitEnd(clazz, instance);
            } catch (Throwable ex) {
                new ClassVisitException(clazz, group, ex).printStackTrace();
            }
        }
    }

}
