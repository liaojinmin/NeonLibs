package me.neon.libs.taboolib.cmd

import me.neon.libs.core.LifeCycle
import me.neon.libs.core.inject.Awake
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.*
import org.bukkit.permissions.Permission
import org.bukkit.plugin.Plugin
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import org.tabooproject.reflex.Reflex.Companion.setProperty
import java.lang.reflect.Constructor

/**
 * NeonMail
 * me.neon.mail.libs.command
 *
 * @author 老廖
 * @since 2024/2/14 23:25
 */
object CommandLoader {

    private var isSupportedUnknownCommand = false

    private val registeredCommands = HashMap<Plugin, MutableList<CommandStructure>>()

    private val commandMap by lazy {
        Bukkit.getPluginManager().getProperty<SimpleCommandMap>("commandMap")!!
    }

    private val knownCommands by lazy {
        commandMap.getProperty<MutableMap<String, Command>>("knownCommands")!!
    }

    private val constructor: Constructor<PluginCommand> by lazy {
        PluginCommand::class.java.getDeclaredConstructor(String::class.java, Plugin::class.java).also {
            it.isAccessible = true
        }
    }

    fun registerCommand(
        plugin: Plugin,
        command: CommandStructure,
        executor: CommandExecutor,
        completer: CommandCompleter,
     //   commandBuilder: CommandBase.() -> Unit,
    ) {
        val pluginCommand = constructor.newInstance(command.name, plugin)
        pluginCommand.setExecutor { sender, _, label, args ->
            executor.execute(sender, command, label, args)
        }
        pluginCommand.setTabCompleter { sender, _, label, args ->
            completer.execute(sender, command, label, args) ?: emptyList()
        }
        val permission = command.permission.ifEmpty { "${plugin.name.lowercase()}.command.${command.name}.use" }
        // 修改属性
        pluginCommand.setProperty("description", command.description.ifEmpty { command.name })
        pluginCommand.setProperty("usageMessage", command.usage)
        pluginCommand.setProperty("aliases", command.aliases)
        pluginCommand.setProperty("activeAliases", command.aliases)
        pluginCommand.setProperty("permission", permission)

        val permissionMessage = command.permissionMessage.ifEmpty { "§c你没有权限使用..." }
        try {
            pluginCommand.setProperty("permissionMessage", permissionMessage)
        } catch (ex: Exception) {
            pluginCommand.setProperty("permissionMessage", Component.text(permission))
        }

        registerPermission(permission, command.permissionDefault)

        command.permissionChildren.forEach {
            registerPermission(it.key, it.value)
        }

        // 注册命令
        knownCommands.remove(command.name)
        knownCommands["${plugin.name.lowercase()}:${pluginCommand.name}"] = pluginCommand
        knownCommands[pluginCommand.name] = pluginCommand
        pluginCommand.aliases.forEach {
            knownCommands[it] = pluginCommand
        }
        pluginCommand.register(commandMap)

        runCatching {
            if (pluginCommand.getProperty<Any>("timings") == null) {
                val timingsManager = Class.forName("co.aikar.timings.TimingsManager")
                pluginCommand.setProperty("timings", timingsManager.invokeMethod("getCommandTiming", plugin.name, pluginCommand))
            }
        }
        sync()
        registeredCommands.computeIfAbsent(plugin) { mutableListOf() }.add(command)

        /*
        registeredCommands.compute(plugin) { _, value ->
            value?.also { it.add(command) } ?: mutableListOf<CommandStructure>().apply { add(command) }
        }

         */
    }

    fun unregisterCommand(plugin: Plugin) {
        registeredCommands.remove(plugin)?.forEach {
            unregister(it.name)
            it.aliases.forEach { a -> unregister(a) }
        }
        sync()
    }

    @Awake(LifeCycle.DISABLE)
    fun unregisterCommands() {
        registeredCommands.values.forEach {
            it.forEach { da ->
                unregister(da.name)
                da.aliases.forEach { a -> unregister(a) }
            }
        }
        sync()
        registeredCommands.clear()
    }

    private fun unregister(command: String) {
        knownCommands.remove(command)
        sync()
    }

    // 注册权限
    private fun registerPermission(permission: String, default: PermissionDefault) {
        if (Bukkit.getPluginManager().getPermission(permission) == null) {
            try {
                val p = Permission(permission, org.bukkit.permissions.PermissionDefault.values()[default.ordinal])
                Bukkit.getPluginManager().addPermission(p)
                Bukkit.getPluginManager().recalculatePermissionDefaults(p)
                p.recalculatePermissibles()
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    private fun sync() {
        // 1.13 sync commands
        runCatching {
            Bukkit.getServer().invokeMethod<Void>("syncCommands")
            Bukkit.getOnlinePlayers().forEach { it.invokeMethod<Void>("updateCommands")}
            isSupportedUnknownCommand = true
        }
    }

}