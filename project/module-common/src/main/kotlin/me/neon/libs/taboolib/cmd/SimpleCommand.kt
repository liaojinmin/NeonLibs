package me.neon.libs.taboolib.cmd

import me.neon.libs.taboolib.cmd.component.CommandBase
import me.neon.libs.taboolib.cmd.component.CommandComponent
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandHeader(
    val name: String,
    val aliases: Array<String> = [],
    val description: String = "",
    val usage: String = "",
    val permission: String = "",
    val permissionMessage: String = "",
    val permissionDefault: PermissionDefault = PermissionDefault.OP,
    val newParser: Boolean = false,
)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandBody(
    val aliases: Array<String> = [],
    val optional: Boolean = false,
    val permission: String = "",
    val permissionDefault: PermissionDefault = PermissionDefault.OP,
    val hidden: Boolean = false,
)

fun mainCommand(func: CommandBase.() -> Unit): SimpleCommandMain {
    return SimpleCommandMain(func)
}

fun subCommand(func: CommandComponent.() -> Unit): SimpleCommandBody {
    return SimpleCommandBody(func)
}

class SimpleCommandMain(val func: CommandBase.() -> Unit = {})

class SimpleCommandBody(val func: CommandComponent.() -> Unit = {}) {

    var name = ""
    var aliases = emptyArray<String>()
    var optional = false
    var permission = ""
    var permissionDefault: PermissionDefault = PermissionDefault.OP
    var hidden = false
    val children = ArrayList<SimpleCommandBody>()

    override fun toString(): String {
        return "SimpleCommandBody(name='$name', children=$children)"
    }
}
/**
 * 注册一个命令
 *
 * @param name 命令名
 * @param aliases 命令别名
 * @param description 命令描述
 * @param usage 命令用法
 * @param permission 命令权限
 * @param permissionMessage 命令权限提示
 * @param permissionDefault 命令权限默认值
 * @param permissionChildren 命令权限子节点
 * @param commandBuilder 命令构建器
 */
fun command(
    plugin: Plugin,
    name: String,
    aliases: List<String> = emptyList(),
    description: String = "",
    usage: String = "",
    permission: String = "",
    permissionMessage: String = "",
    permissionDefault: PermissionDefault = PermissionDefault.OP,
    permissionChildren: Map<String, PermissionDefault> = emptyMap(),
    newParser: Boolean = false,
    commandBuilder: CommandBase.() -> Unit,
) {

    CommandLoader.registerCommand(plugin,
        // 创建命令结构
        CommandStructure(name, aliases, description, usage, permission, permissionMessage, permissionDefault, permissionChildren),
        // 创建执行器
        object : CommandExecutor {

            override fun execute(sender: CommandSender, command: CommandStructure, name: String, args: Array<String>): Boolean {
                val commandBase = CommandBase().also(commandBuilder)
                return commandBase.execute(CommandContext(sender, command, name, commandBase, newParser, args))
            }
        },
        // 创建补全器
        object : CommandCompleter {

            override fun execute(sender: CommandSender, command: CommandStructure, name: String, args: Array<String>): List<String>? {
                val commandBase = CommandBase().also(commandBuilder)
                return commandBase.suggest(CommandContext(sender, command, name, commandBase, newParser, args))
            }
        },
        // 传入原始命令构建器
        //commandBuilder
    )
}

