package me.neon.libs.taboolib.core.inject;


import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * TabooLib
 * taboolib.common.inject.VisitorGroup
 *
 * @author sky
 * @since 2021/8/14 12:18 上午
 */
public class VisitorGroup {

    private final byte priority;
    private final List<ClassVisitor> list = new CopyOnWriteArrayList<>();

    public VisitorGroup(byte priority) {
        this.priority = priority;
    }

    /**
     * 获取所有依赖注入接口
     *
     * @return 所有 ClassVisitor
     */
    public List<ClassVisitor> getAll() {
        return list;
    }

    /**
     * 通过生命周期获取所有依赖注入接口
     */
    public List<ClassVisitor> get() {
        return new LinkedList<>(list);
    }

    /**
     * 获取优先级
     *
     * @return 优先级
     */
    public byte getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return "VisitorGroup{" +
                "priority=" + priority +
                ", list=" + list +
                '}';
    }
}