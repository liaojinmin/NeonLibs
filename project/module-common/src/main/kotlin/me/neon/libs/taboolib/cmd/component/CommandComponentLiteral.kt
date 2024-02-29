package me.neon.libs.taboolib.cmd.component

class CommandComponentLiteral(
    val aliases: Array<String>,
    val hidden: Boolean,
    index: Int,
    optional: Boolean,
    permission: String
) : CommandComponent(index, optional, permission) {

    override fun toString(): String {
        return "CommandComponentLiteral(aliases=${aliases.contentToString()})"
    }
}