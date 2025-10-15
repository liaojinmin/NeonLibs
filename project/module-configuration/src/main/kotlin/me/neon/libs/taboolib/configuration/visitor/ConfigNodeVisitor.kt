package me.neon.libs.taboolib.configuration.visitor

import me.neon.libs.NeonLibsLoader
import me.neon.libs.NeonLibsLoader.warning
import me.neon.libs.core.LifeCycle
import me.neon.libs.core.inject.ClassVisitor
import me.neon.libs.core.inject.Instance
import me.neon.libs.core.inject.Visitor
import me.neon.libs.taboolib.configuration.ConfigNodeTransfer
import me.neon.libs.taboolib.configuration.annotation.ConfigNode
import me.neon.libs.util.Coerce
import org.bukkit.plugin.Plugin
import org.tabooproject.reflex.ClassField
import java.util.function.Supplier

@Visitor
@Instance
class ConfigNodeVisitor : ClassVisitor(2) {

    init {
        NeonLibsLoader.info("正在初始化 ConfigNodeVisitor 注入器...")
    }

    override fun visitField(plugin: Plugin, field: ClassField, clazz: Class<*>, instance: Supplier<*>?) {
        if (field.isAnnotationPresent(ConfigNode::class.java)) {
            val node = field.getAnnotation(ConfigNode::class.java)
            var bind = node.property("bind", "")
            // 获取默认文件名称
            if (bind.isEmpty() || bind == "config.yml") {
                bind = field.getAnnotationIfPresent(ConfigNode::class.java)?.property("bind") ?: "config.yml"
            }
            // 自动补全后缀
            if (!bind.contains('.')) {
                bind = "$bind.yml"
            }
            val file = ConfigVisitor.files[bind]
            if (file == null) {
                warning(
                    """
                        $bind 没有定义: $field
                        $bind not defined: $field
                    """
                )
                return
            }
            file.nodes += field
            // 绑定的节点
            val bindNode = node.property("value", "").ifEmpty { field.name.substringBefore('$').toNode() }
            var data = file.configuration[bindNode]
            if (data == null) {
                warning(
                    """
                        $bind 中未找到 $bindNode 节点。
                        $bindNode not found in $bind.
                    """
                )
                return
            }
            // 类型转换工具
            if (field.fieldType == ConfigNodeTransfer::class.java) {
                val transfer = field.get(instance?.get()) as ConfigNodeTransfer<*, *>
                transfer.reset(data)
            } else {
                // 基本类型转换
                data = when (field.fieldType) {
                    Integer::class.java -> Coerce.toInteger(data)
                    Character::class.java -> Coerce.toChar(data)
                    java.lang.Byte::class.java -> Coerce.toByte(data)
                    java.lang.Long::class.java -> Coerce.toLong(data)
                    java.lang.Double::class.java -> Coerce.toDouble(data)
                    java.lang.Float::class.java -> Coerce.toFloat(data)
                    java.lang.Short::class.java -> Coerce.toShort(data)
                    java.lang.Boolean::class.java -> Coerce.toBoolean(data)
                    else -> data
                }
                field.set(instance?.get(), data)
            }
        }
    }

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.NONE
    }

    fun String.toNode(): String {
        return map { if (it.isUpperCase()) "-${it.lowercase()}" else it }.joinToString("")
    }
}