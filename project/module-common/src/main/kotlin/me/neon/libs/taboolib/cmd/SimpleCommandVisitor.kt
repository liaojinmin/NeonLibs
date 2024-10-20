package me.neon.libs.taboolib.cmd

import me.neon.libs.taboolib.cmd.CommandLoader.unregisterCommand
import me.neon.libs.taboolib.cmd.component.CommandComponent
import me.neon.libs.core.LifeCycle
import org.bukkit.plugin.Plugin
import org.tabooproject.reflex.ClassField
import org.tabooproject.reflex.ReflexClass
import java.util.function.Supplier
import me.neon.libs.NeonLibsLoader
import me.neon.libs.core.inject.*


/**
 * NeonLibs
 * me.neon.libs.taboolib.cmd
 *
 * @author 老廖
 * @since 2024/2/23 16:28
 */

@Visitor
@Instance
class SimpleCommandVisitor: ClassVisitor(1) {

    init {
        NeonLibsLoader.info("正在初始化 SimpleCommandVisitor 注入器...")
    }

    private val main = HashMap<Plugin, HashMap<String, SimpleCommandMain>>()

    private val body = HashMap<Plugin, HashMap<String, MutableList<SimpleCommandBody>>>()


    override fun visitUnload(plugin: Plugin) {
        // 卸载指令
        NeonLibsLoader.print("[" + plugin.name + "] 正在注销指令...")
        unregisterCommand(plugin)
        main.remove(plugin)
        body.remove(plugin)
    }

    override fun visitField(plugin: Plugin, field: ClassField, clazz: Class<*>, instance: Supplier<*>?) {
        if (field.isAnnotationPresent(CommandBody::class.java) && field.fieldType == SimpleCommandMain::class.java) {
            //NeonLibsLoader.print("[" + plugin.name + "] 正在注册指令...")
            //NeonLibsLoader.print("    类: $clazz 字段: ${field.name}")
            main.computeIfAbsent(plugin) {
                HashMap()
            }.also {
                it[clazz.name] = field.get(instance?.get()) as SimpleCommandMain
            }
        } else {
            body.computeIfAbsent(plugin) {
                HashMap()
            }.also {
                it.computeIfAbsent(clazz.name) { ArrayList() } += loadBody(field, instance) ?: return
            }
        }
    }

    private fun register(body: SimpleCommandBody, component: CommandComponent) {
        component.literal(body.name, *body.aliases, optional = body.optional, permission = body.permission, hidden = body.hidden) {
            if (body.children.isEmpty()) {
                body.func(this)
            } else {
                body.children.forEach { children ->
                    register(children, this)
                }
            }
        }
    }

    override fun visitEnd(plugin: Plugin, clazz: Class<*>, instance: Supplier<*>?) {
        if (clazz.isAnnotationPresent(CommandHeader::class.java)) {
            //NeonLibsLoader.print("[" + plugin.name + "] 正在注册指令...")
            //NeonLibsLoader.print("    类: $clazz")
            val annotation = clazz.getAnnotation(CommandHeader::class.java)
            command(
                plugin,
                annotation.name,
                annotation.aliases.toList(),
                annotation.description,
                annotation.usage,
                annotation.permission,
                annotation.permissionMessage,
                annotation.permissionDefault,
                body[plugin]?.get(clazz.name)?.filter { it.permission.isNotEmpty() }
                    ?.associate { it.permission to it.permissionDefault } ?: emptyMap(),
                annotation.newParser,
            ) {
                main[plugin]?.get(clazz.name)?.func?.invoke(this)
                body[plugin]?.get(clazz.name)?.forEach { body ->

                    register(body, this)
                }
            }
        }
    }

    private fun loadBody(field: ClassField, instance: Any?): SimpleCommandBody? {
        if (field.isAnnotationPresent(CommandBody::class.java)) {
            val annotation = field.getAnnotation(CommandBody::class.java)
            val obj = field.get(instance)
            return when (field.fieldType) {
                SimpleCommandMain::class.java -> {
                    null
                }
                SimpleCommandBody::class.java -> {
                    (obj as SimpleCommandBody).apply {
                        name = field.name
                        aliases = annotation.property("aliases", emptyArray())
                        optional = annotation.property("optional", false)
                        permission = annotation.property("permission", "")
                        permissionDefault = annotation.enum("permissionDefault", PermissionDefault.OP)
                        hidden = annotation.property("hidden", false)
                    }
                }
                else -> {
                    SimpleCommandBody().apply {
                        name = field.name
                        aliases = annotation.property("aliases", emptyArray())
                        optional = annotation.property("optional", false)
                        permission = annotation.property("permission", "")
                        permissionDefault = annotation.enum("permissionDefault", PermissionDefault.OP)
                        hidden = annotation.property("hidden", false)
                        ReflexClass.of(field.fieldType).structure.fields.forEach {
                            children += loadBody(it, instance) ?: return@forEach
                        }
                    }
                }
            }
        }
        return null
    }

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.ENABLE
    }




}