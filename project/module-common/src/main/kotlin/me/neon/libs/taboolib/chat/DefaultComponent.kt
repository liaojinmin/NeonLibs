package me.neon.libs.taboolib.chat

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.*
import net.md_5.bungee.api.chat.hover.content.Entity
import net.md_5.bungee.api.chat.hover.content.Item
import net.md_5.bungee.api.chat.hover.content.Text
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.awt.Color

/**
 * TabooLib
 * me.neon.mail.libs.chat.DefaultComponent
 *
 * @author 坏黑
 * @since 2023/2/9 20:36
 */
class DefaultComponent() : Component {

    constructor(from: List<BaseComponent>) : this() {
        left.addAll(from)
    }

    private val left = arrayListOf<BaseComponent>()
    private val latest = arrayListOf<BaseComponent>()
    private val component: BaseComponent
        get() = when {
            left.isEmpty() && latest.size == 1 -> latest[0]
            latest.isEmpty() && left.size == 1 -> left[0]
            else -> TextComponent(*(left + latest).toTypedArray())
        }

    init {
        color(StandardColors.RESET)
    }

    override fun toRawMessage(): String {
        return ComponentSerializer.toString(component)
    }

    override fun toLegacyText(): String {
        return Component.toLegacyString(*(left + latest).toTypedArray())
    }

    override fun toPlainText(): String {
        return TextComponent.toPlainText(*(left + latest).toTypedArray())
    }

    override fun broadcast() {
        Bukkit.getOnlinePlayers().forEach { sendTo(it) }
    }

    override fun sendTo(sender: CommandSender) {
        if (sender is Player) {
            sender.spigot().sendMessage(*ComponentSerializer.parse(toRawMessage()))
        } else {
            sender.sendMessage(toLegacyText())
        }
    }

    override fun newLine(): Component {
        return append("\n")
    }

    override fun plusAssign(text: String) {
        append(text)
    }

    override fun plusAssign(other: Component) {
        append(other)
    }

    override fun append(text: String): Component {
        flush()
        latest += try {
            TextComponent.fromLegacyText(text, ChatColor.RESET)
        } catch (_: NoSuchMethodError) {
            TextComponent.fromLegacyText("${ChatColor.RESET}$text")
        }
        return this
    }

    override fun append(other: Component): Component {
        other as? DefaultComponent ?: throw RuntimeException()
        flush()
        latest += other.component
        return this
    }

    override fun appendTranslation(text: String, vararg obj: Any): Component {
        return appendTranslation(text, obj.toList())
    }

    override fun appendTranslation(text: String, obj: List<Any>): Component {
        flush()
        latest += TranslatableComponent(text, obj.map { if (it is DefaultComponent) it.component else it })
        return this
    }

    override fun appendKeybind(key: String): Component {
        flush()
        latest += KeybindComponent(key)
        return this
    }

    override fun appendScore(name: String, objective: String): Component {
        flush()
        latest += ScoreComponent(name, objective)
        return this
    }

    override fun appendSelector(selector: String): Component {
        flush()
        latest += SelectorComponent(selector)
        return this
    }

    override fun hoverText(text: String): Component {
        return hoverText(Component.text(text))
    }

    override fun hoverText(text: List<String>): Component {
        val component = Component.empty()
        text.forEachIndexed { index, s ->
            component.append(s)
            if (index != text.size - 1) {
                component.newLine()
            }
        }
        return hoverText(component)
    }

    override fun hoverText(text: Component): Component {
        text as? DefaultComponent ?: error("Unsupported component type.")
        try {
            latest.forEach { it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(arrayOf(text.component))) }
        } catch (_: NoClassDefFoundError) {
            latest.forEach { it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(text.component)) }
        } catch (_: NoSuchMethodError) {
            latest.forEach { it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(text.component)) }
        }
        return this
    }

    override fun hoverItem(id: String, nbt: String): Component {
        try {
            latest.forEach { it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ITEM, Item(id, 1, ItemTag.ofNbt(nbt))) }
        } catch (_: NoClassDefFoundError) {
            latest.forEach { it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ITEM, ComponentBuilder("{id:\"$id\",Count:1b,tag:$nbt}").create()) }
        } catch (_: NoSuchMethodError) {
            latest.forEach { it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ITEM, ComponentBuilder("{id:\"$id\",Count:1b,tag:$nbt}").create()) }
        }
        return this
    }

    override fun hoverEntity(id: String, type: String?, name: String?): Component {
        try {
            val component = if (name != null) TextComponent(name) else null
            latest.forEach { it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ENTITY, Entity(type, id, component)) }
        } catch (_: NoClassDefFoundError) {
            TODO("Unsupported hover entity for this version.")
        }
        return this
    }

    override fun hoverEntity(id: String, type: String?, name: Component?): Component {
        try {
            val component = if (name is DefaultComponent) name.component else null
            latest.forEach { it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ENTITY, Entity(type, id, component)) }
        } catch (_: NoClassDefFoundError) {
            TODO("Unsupported hover entity for this version.")
        }
        return this
    }

    override fun click(action: ChatClickAction, value: String): Component {
        when (action) {
            ChatClickAction.OPEN_URL,
            ChatClickAction.OPEN_FILE,
            ChatClickAction.RUN_COMMAND,
            ChatClickAction.SUGGEST_COMMAND,
            ChatClickAction.CHANGE_PAGE,
            ChatClickAction.COPY_TO_CLIPBOARD -> latest.forEach { it.clickEvent = ClickEvent(ClickEvent.Action.valueOf(action.name), value) }
            // 插入文本
            ChatClickAction.INSERTION -> clickInsertText(value)
        }
        return this
    }

    override fun clickOpenURL(url: String): Component {
        return click(ChatClickAction.OPEN_URL, url)
    }

    override fun clickOpenFile(file: String): Component {
        return click(ChatClickAction.OPEN_FILE, file)
    }

    override fun clickRunCommand(command: String): Component {
        return click(ChatClickAction.RUN_COMMAND, command)
    }

    override fun clickSuggestCommand(command: String): Component {
        return click(ChatClickAction.SUGGEST_COMMAND, command)
    }

    override fun clickChangePage(page: Int): Component {
        return click(ChatClickAction.CHANGE_PAGE, page.toString())
    }

    override fun clickCopyToClipboard(text: String): Component {
        return click(ChatClickAction.COPY_TO_CLIPBOARD, text)
    }

    override fun clickInsertText(text: String): Component {
        latest.forEach { it.insertion = text }
        return this
    }

    override fun decoration(decoration: ChatDecoration): Component {
        when (decoration) {
            ChatDecoration.BOLD -> bold()
            ChatDecoration.ITALIC -> italic()
            ChatDecoration.UNDERLINE -> underline()
            ChatDecoration.STRIKETHROUGH -> strikethrough()
            ChatDecoration.OBFUSCATED -> obfuscated()
        }
        return this
    }

    override fun undecoration(decoration: ChatDecoration): Component {
        when (decoration) {
            ChatDecoration.BOLD -> unbold()
            ChatDecoration.ITALIC -> unitalic()
            ChatDecoration.UNDERLINE -> ununderline()
            ChatDecoration.STRIKETHROUGH -> unstrikethrough()
            ChatDecoration.OBFUSCATED -> unobfuscated()
        }
        return this
    }

    override fun undecoration(): Component {
        unbold()
        unitalic()
        ununderline()
        unstrikethrough()
        unobfuscated()
        return this
    }

    override fun bold(): Component {
        latest.forEach { it.isBold = true }
        return this
    }

    override fun unbold(): Component {
        latest.forEach { it.isBold = false }
        return this
    }

    override fun italic(): Component {
        latest.forEach { it.isItalic = true }
        return this
    }

    override fun unitalic(): Component {
        latest.forEach { it.isItalic = false }
        return this
    }

    override fun underline(): Component {
        latest.forEach { it.isUnderlined = true }
        return this
    }

    override fun ununderline(): Component {
        latest.forEach { it.isUnderlined = false }
        return this
    }

    override fun strikethrough(): Component {
        latest.forEach { it.isStrikethrough = true }
        return this
    }

    override fun unstrikethrough(): Component {
        latest.forEach { it.isStrikethrough = false }
        return this
    }

    override fun obfuscated(): Component {
        latest.forEach { it.isObfuscated = true }
        return this
    }

    override fun unobfuscated(): Component {
        latest.forEach { it.isObfuscated = false }
        return this
    }

    override fun font(font: String): Component {
        latest.forEach { it.font = font }
        return this
    }

    override fun unfont(): Component {
        latest.forEach { it.font = null }
        return this
    }

    override fun color(color: StandardColors): Component {
        latest.forEach { it.color = color.toChatColor() }
        return this
    }

    override fun color(color: Color): Component {
        latest.forEach { it.color = ChatColor.of(color) }
        return this
    }

    override fun uncolor(): Component {
        latest.forEach { it.color = null }
        return this
    }

    override fun toSpigotObject(): BaseComponent {
        return component
    }

    override fun toLegacyRawMessage(): RawMessage {
        return RawMessage(this)
    }

    /** 释放缓冲区 */
    fun flush() {
        left.addAll(latest)
        latest.clear()
    }

    override fun toString(): String {
        return toRawMessage()
    }
}