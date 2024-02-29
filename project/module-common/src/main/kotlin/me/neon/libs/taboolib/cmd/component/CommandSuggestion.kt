package me.neon.libs.taboolib.cmd.component

import me.neon.libs.taboolib.cmd.CommandContext
import org.bukkit.command.CommandSender

class CommandSuggestion(bind: Class<CommandSender>, val uncheck: Boolean, val function: (sender: CommandSender, context: CommandContext) -> List<String>?) : CommandBinder(bind) {

    @Suppress("UNCHECKED_CAST")
    fun exec(context: CommandContext): List<String>? {
        val sender = cast(context)
        return if (sender != null) {
            function.invoke(sender, context.copy(sender = sender))
        } else {
            null
        }
    }
}