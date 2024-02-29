package me.neon.libs.taboolib.cmd

import me.neon.libs.taboolib.cmd.component.CommandComponent
import me.neon.libs.taboolib.core.inject.ClassVisitor
import org.bukkit.plugin.Plugin
import org.tabooproject.reflex.ClassField
import org.tabooproject.reflex.ReflexClass
import java.util.function.Supplier

/**
 * NeonLibs
 * me.neon.libs.taboolib.cmd
 *
 * @author 老廖
 * @since 2024/2/23 16:28
 */
@Suppress("DuplicatedCode")
class SimpleCommandRegister: ClassVisitor(0) {

    private val main = HashMap<String, SimpleCommandMain>()
    private val body = HashMap<String, MutableList<SimpleCommandBody>>()

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

    override fun visit(plugin: Plugin, field: ClassField, clazz: Class<*>, instance: Supplier<*>?) {
        if (field.isAnnotationPresent(CommandBody::class.java) && field.fieldType == SimpleCommandMain::class.java) {
            main[clazz.name] = field.get(instance?.get()) as SimpleCommandMain
        } else {
            body.computeIfAbsent(clazz.name) { ArrayList() } += loadBody(field, instance) ?: return
        }
    }

    override fun visitEnd(plugin: Plugin, clazz: Class<*>, instance: Supplier<*>?) {
        if (clazz.isAnnotationPresent(CommandHeader::class.java)) {
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
                body[clazz.name]?.filter { it.permission.isNotEmpty() }
                    ?.associate { it.permission to it.permissionDefault } ?: emptyMap(),
                annotation.newParser,
            ) {
                main[clazz.name]?.func?.invoke(this)
                body[clazz.name]?.forEach { body ->
                    fun register(body: SimpleCommandBody, component: CommandComponent) {
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
                    register(body, this)
                }
            }
        }
    }


}