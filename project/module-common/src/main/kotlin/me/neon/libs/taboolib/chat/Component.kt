package me.neon.libs.taboolib.chat

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.*
import net.md_5.bungee.chat.ComponentSerializer
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import java.awt.Color

/**
 * TabooLib
 * me.neon.mail.libs.chat.ComponentText
 *
 * @author 坏黑
 * @since 2023/2/9 20:17
 */
@Suppress("SpellCheckingInspection")
interface Component : Source {

    /** 换行 */
    fun newLine(): Component

    /** 添加文本块 */
    operator fun plusAssign(text: String)

    /** 追加另一个 [Component] */
    operator fun plusAssign(other: Component)

    /** 添加文本块 */
    fun append(text: String): Component

    /** 追加另一个 [Component] */
    fun append(other: Component): Component

    /** 添加翻译文本块 */
    fun appendTranslation(text: String, vararg obj: Any): Component

    /** 添加翻译文本块 */
    fun appendTranslation(text: String, obj: List<Any>): Component

    /** 添加按键文本块 */
    fun appendKeybind(key: String): Component

    /** 添加分数文本块 */
    fun appendScore(name: String, objective: String): Component

    /** 添加选择器文本块 */
    fun appendSelector(selector: String): Component

    /** 显示文本 */
    fun hoverText(text: String): Component

    /** 显示多行文本 */
    fun hoverText(text: List<String>): Component

    /** 显示 [Component] */
    fun hoverText(text: Component): Component

    /** 显示物品 */
    fun hoverItem(id: String, nbt: String = "{}"): Component

    /** 显示实体 */
    fun hoverEntity(id: String, type: String? = null, name: String? = null): Component

    /** 显示实体 */
    fun hoverEntity(id: String, type: String? = null, name: Component? = null): Component

    /** 交互行为 */
    fun click(action: ChatClickAction, value: String): Component

    /** 打开链接 */
    fun clickOpenURL(url: String): Component

    /** 打开文件 */
    fun clickOpenFile(file: String): Component

    /** 执行命令 */
    fun clickRunCommand(command: String): Component

    /** 建议命令 */
    fun clickSuggestCommand(command: String): Component

    /** 切换页码 */
    fun clickChangePage(page: Int): Component

    /** 复制文本 */
    fun clickCopyToClipboard(text: String): Component

    /** 插入文本 */
    fun clickInsertText(text: String): Component

    /** 添加装饰 */
    fun decoration(decoration: ChatDecoration): Component

    /** 移除装饰 */
    fun undecoration(decoration: ChatDecoration): Component

    /** 移除所有装饰 */
    fun undecoration(): Component

    /** 加粗 */
    fun bold(): Component

    /** 移除加粗 */
    fun unbold(): Component

    /** 斜体 */
    fun italic(): Component

    /** 移除斜体 */
    fun unitalic(): Component

    /** 下划线 */
    fun underline(): Component

    /** 移除下划线 */
    fun ununderline(): Component

    /** 删除线 */
    fun strikethrough(): Component

    /** 移除删除线 */
    fun unstrikethrough(): Component

    /** 模糊 */
    fun obfuscated(): Component

    /** 移除模糊 */
    fun unobfuscated(): Component

    /** 添加字体 */
    fun font(font: String): Component

    /** 移除字体 */
    fun unfont(): Component

    /** 添加颜色 */
    fun color(color: StandardColors): Component

    /** 添加颜色 */
    fun color(color: Color): Component

    /** 移除颜色 */
    fun uncolor(): Component

    /** 转换为 Spigot 对象 */
    fun toSpigotObject(): BaseComponent

    /** 转换为 RawMessage */
    fun toLegacyRawMessage(): RawMessage

    companion object {

        /** 创建空白块 */
        fun empty(): Component {
            return DefaultComponent()
        }

        /** 创建文本块 */
        fun text(text: String): Component {
            return DefaultComponent().append(text)
        }

        /** 创建分数文本块 */
        fun score(name: String, objective: String): Component {
            return DefaultComponent().appendScore(name, objective)
        }

        /** 创建按键文本块 */
        fun keybind(key: String): Component {
            return DefaultComponent().appendKeybind(key)
        }

        /** 创建选择器文本块 */
        fun selector(selector: String): Component {
            return DefaultComponent().appendSelector(selector)
        }

        /** 创建翻译文本块 */
        fun translation(text: String, vararg obj: Any): Component{
            return DefaultComponent().appendTranslation(text, *obj)
        }

        /** 创建翻译文本块 */
        fun translation(text: String, obj: List<Any>): Component{
            return DefaultComponent().appendTranslation(text, obj)
        }

        /** 从原始信息中读取 */
        fun parseRaw(text: String): Component{
            return DefaultComponent(ComponentSerializer.parse(text).toList())
        }

        fun toLegacyString(vararg components: BaseComponent): String {
            val builder = StringBuilder()
            val newArray: Array<out BaseComponent> = components
            val size = components.size
            for (i in 0 until size) {
                builder.append(toLegacyString(newArray[i]))
            }
            return builder.toString()
        }

        private fun toLegacyString(component: BaseComponent): String {
            val builder = StringBuilder()
            toLegacyString1(component, builder)
            return builder.toString()
        }

        private fun toLegacyString1(component: BaseComponent, builder: StringBuilder): String {
            if (component is TranslatableComponent) {
                component.invokeMethod("toLegacyText", builder)
            } else {
                addFormat(component, builder)
                when (component) {
                    is TextComponent -> builder.append(component.text)
                    is KeybindComponent -> builder.append(component.keybind)
                    is ScoreComponent -> builder.append(component.value)
                    is SelectorComponent -> builder.append(component.selector)
                }
            }
            component.extra?.forEach { toLegacyString1(it, builder) }
            return builder.toString()
        }

        private fun addFormat(component: BaseComponent, builder: StringBuilder) {
            if (component.colorRaw != null) {
                builder.append(component.color)
            }
            if (component.isBold) {
                builder.append(ChatColor.BOLD)
            }
            if (component.isItalic) {
                builder.append(ChatColor.ITALIC)
            }
            if (component.isUnderlined) {
                builder.append(ChatColor.UNDERLINE)
            }
            if (component.isStrikethrough) {
                builder.append(ChatColor.STRIKETHROUGH)
            }
            if (component.isObfuscated) {
                builder.append(ChatColor.MAGIC)
            }
        }
    }
}