package me.neon.libs.event

import me.neon.libs.NeonLibsLoader
import me.neon.libs.core.LifeCycle
import me.neon.libs.core.inject.ClassVisitor
import org.bukkit.plugin.Plugin
import org.tabooproject.reflex.ClassMethod
import org.tabooproject.reflex.Unknown
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier


class EventRegisterVisitor : ClassVisitor(0) {

    private val listenEvents = ConcurrentHashMap<Plugin, ArrayList<BukkitListener>>()

    private val sefiEvents = ConcurrentHashMap<Plugin, ArrayList<InternalListener>>()

    private fun addEventCache(plugin: Plugin, bukkitListener: BukkitListener) {
        listenEvents.computeIfAbsent(plugin) { ArrayList()
        }.add(bukkitListener)
    }

    private fun addEventCache(plugin: Plugin, sefi: InternalListener) {
        sefiEvents.computeIfAbsent(plugin) { ArrayList()
        }.add(sefi)
    }

    override fun visitUnload(plugin: Plugin) {
        // 取消事件
        NeonLibsLoader.print("[" + plugin.name + "] 正在注销事件...")
        listenEvents.remove(plugin)?.forEach { unregisterListener(it) }
        sefiEvents.remove(plugin)?.forEach { it.cancel() }
    }

    @Suppress("UNCHECKED_CAST")
    override fun visitMethod(plugin: Plugin, method: ClassMethod, clazz: Class<*>, instance: Supplier<*>?) {
        if (method.isAnnotationPresent(SubscribeEvent::class.java) && method.parameter.size == 1) {
            //NeonLibsLoader.print("[" + plugin.name + "] 正在注册事件...")
            //NeonLibsLoader.print("    类: $clazz 方法: ${method.name}")
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
                NeonLibsLoader.warning("${method.parameter[0].name} not found...")
                return
            }

            val obj = instance?.get()
            val priority = anno.enum<EventPriority>("priority", EventPriority.NORMAL)
            val ignoreCancelled = anno.property("ignoreCancelled", false)
            val listenType = method.parameterTypes[0]
            //NeonLibsLoader.info("注册事件监听器 $listenType")

            // 内部事件处理
            if (InternalEvent::class.java.isAssignableFrom(listenType)) {
                addEventCache(plugin,
                    InternalEventBus.listen(
                        listenType as Class<InternalEvent>,
                        priority.level,
                        ignoreCancelled
                    ) { invoke(obj, method, it) })
                return
            }
            if (listenType == OptionalEvent::class.java) {
                if (optionalEvent != null) {
                    addEventCache(plugin,
                        registerListener(
                            plugin,
                            optionalEvent,
                            org.bukkit.event.EventPriority.values()[priority.ordinal],
                            ignoreCancelled
                        ) {
                            invoke(instance?.get(), method, it, true)
                        }
                    )
                }
            } else {
                addEventCache(plugin,
                    registerListener(plugin,
                        listenType,
                        org.bukkit.event.EventPriority.values()[priority.ordinal],
                        ignoreCancelled
                    ) {
                        invoke(instance?.get(), method, it, false)
                    }
                )
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

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.ENABLE
    }

}