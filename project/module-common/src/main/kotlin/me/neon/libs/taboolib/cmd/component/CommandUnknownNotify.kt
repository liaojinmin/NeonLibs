package me.neon.libs.taboolib.cmd.component

import me.neon.libs.taboolib.cmd.CommandContext
import org.bukkit.command.CommandSender

class CommandUnknownNotify(bind: Class<CommandSender>, val function: (sender: CommandSender, context: CommandContext, index: Int, state: Int) -> Unit) : CommandBinder(bind) {

    @Suppress("UNCHECKED_CAST")
    fun exec(context: CommandContext, index: Int, state: Int) {
        val sender = cast(context)
        if (sender != null) {
            function.invoke(sender, context.copy(sender = sender), index, state)
        }
    }
}