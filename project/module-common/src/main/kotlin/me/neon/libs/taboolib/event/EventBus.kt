package me.neon.libs.taboolib.event

import me.neon.libs.taboolib.core.inject.ClassVisitor
import me.neon.libs.utils.info
import me.neon.libs.utils.warning
import org.bukkit.plugin.Plugin
import org.tabooproject.reflex.ClassMethod
import org.tabooproject.reflex.Unknown
import java.util.function.Supplier


class EventBus : ClassVisitor(0) {

    @Suppress("UNCHECKED_CAST")
    override fun visit(plugin: Plugin, method: ClassMethod, clazz: Class<*>, instance: Supplier<*>?) {
        if (method.isAnnotationPresent(SubscribeEvent::class.java) && method.parameter.size == 1) {
            val anno = method.getAnnotation(SubscribeEvent::class.java)
            val bind = anno.property("bind", "")
            val optionalEvent = if (bind.isNotEmpty()) {
                try {
                    Class.forName(bind)
                } catch (ex: Throwable) {
                    null
                }
            } else {
                null
            }
            if (method.parameterTypes.size != 1) {
                error("$clazz#${method.name} must have 1 parameter and must be an event type")
            }
            // 未找到事件类
            if (method.parameterTypes[0] == Unknown::class.java) {
                // 警告
                warning("${method.parameter[0].name} not found...")
                return
            }
            val priority = anno.enum<EventPriority>("priority", EventPriority.NORMAL)
            val ignoreCancelled = anno.property("ignoreCancelled", false)
            val listenType = method.parameterTypes[0]

            info("注册事件 -> $listenType")

            if (listenType == OptionalEvent::class.java && optionalEvent != null) {
                registerListener(plugin,
                    optionalEvent,
                    org.bukkit.event.EventPriority.values()[priority.ordinal],
                    ignoreCancelled
                ) {
                    invoke(instance?.get(), method, it, true)
                }
            } else {
                registerListener(plugin,
                    listenType,
                    org.bukkit.event.EventPriority.values()[priority.ordinal],
                    ignoreCancelled
                ) {
                    invoke(instance?.get(), method, it, false)
                }
            }
        }
    }
    private fun invoke(obj: Any?, method: ClassMethod, it: Any, optional: Boolean = false) {
        if (obj != null) {
            method.invoke(obj, if (optional) OptionalEvent(it) else it)
        } else {
            method.invokeStatic(if (optional) OptionalEvent(it) else it)
        }
    }

}