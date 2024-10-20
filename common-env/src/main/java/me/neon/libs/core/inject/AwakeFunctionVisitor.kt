package me.neon.libs.core.inject

import me.neon.libs.core.LifeCycle
import org.bukkit.plugin.Plugin
import org.tabooproject.reflex.ClassMethod

import java.util.function.Supplier

class AwakeFunctionVisitor(private val lifeCycle: LifeCycle) : ClassVisitor(0) {

    override fun visitMethod(plugin: Plugin, method: ClassMethod, clazz: Class<*>, instance: Supplier<*>?) {
        if (method.isAnnotationPresent(Awake::class.java) && method.getAnnotation(Awake::class.java).enum<LifeCycle>("value", LifeCycle.ENABLE) == lifeCycle) {
            if (instance != null) {
                method.invoke(instance.get())
            } else {
                method.invokeStatic()
            }
        }
    }

    override fun getLifeCycle(): LifeCycle {
        return lifeCycle
    }

}