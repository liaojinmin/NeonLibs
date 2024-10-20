package me.neon.libs.util

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.entity.Player


object StringUtils {

    val expiryRegex: Regex by lazy { Regex("\\d+?(?i)(d|h|m|s|天|时|分|秒)\\s?") }

}

fun String.parseStringTimerToLong(): Long {
    if (isEmpty() || this == "-1") return -1
    var timer: Long = 0
    StringUtils.expiryRegex.findAll(this).forEach {
        val data = it.groupValues[0].substringBefore(it.groupValues[1]).toLong()
        timer += when (it.groupValues[1]) {
            "d","天" -> {
                data * 60 * 60 * 24
            }
            "h","时" -> {
                data * 60 * 60
            }
            "m","分" -> {
                data * 60
            }
            else -> data
        }
    }
    return timer * 1000
}

fun String.replacePlaceholder(player: Player): String {
    return try {
        PlaceholderAPI.setPlaceholders(player, this)
    } catch (e: Exception) {
        this
    }
}

fun Collection<String>.replacePlaceholder(player: Player): List<String> {
    return try {
        this.map { PlaceholderAPI.setPlaceholders(player, it) }
    } catch (e: Exception) {
        this.toList()
    }
}

/**
 * 精准的替换字符，可忽略以`&`、`§`起头的颜色字符
 * ```
 * val new = "&B攻击力&7: &f+&666".replaceIgnoreChar("66", "88")
 * println(new) // 应于 "&b攻击力&7: &f+&688" 而不是 "&b攻击力&7: &f+&886"
 * ```
 * final String regex = "(?<![&§])\\d+";
 * @param oldValue 被替换的字符串
 * @param newValue 需要添加的字符串
 * @param pass 以什么字符起头
 * @param offset 右移字符位数
 * @return 新组合
 */
fun String.replaceIgnoreChar(oldValue: String, newValue: String, pass: Array<Char> = arrayOf('§', '&'), offset: Int = 2): String {
    if (isEmpty() || oldValue.isEmpty()) return this
    val builder = StringBuilder(length)
    val chars = toCharArray()
    var i = 0
    while (i < chars.size) {
        val char = chars[i]
        if (pass.contains(char)) {
            builder.append(char).append(chars[i + 1])
            i += offset
        } else if (char == oldValue[0]) {
            var match = true
            for (j in oldValue.indices) {
                if (i + j >= chars.size || chars[i + j] != oldValue[j]) {
                    match = false
                    break
                }
            }
            if (match) {
                builder.append(newValue)
                i += oldValue.length
            } else {
                builder.append(char)
                i++
            }
        } else {
            builder.append(char)
            i++
        }
    }
    return builder.toString()
}

fun String.replaceWithOrder(vararg args: Any): String {
    return replaceWithOrder('{', '}', *args)
}

/**
 * 替换字符串中的变量 {0}, {1}
 */
fun String.replaceWithOrder(start: Char = '{', end: Char = '}', vararg args: Any): String {
    if (args.isEmpty() || isEmpty()) {
        return this
    }
    val chars = toCharArray()
    val builder = StringBuilder(length)
    var i = 0
    while (i < chars.size) {
        val mark = i
        if (chars[i] == start) {
            var num = 0
            val alias = StringBuilder()
            while (i + 1 < chars.size && chars[i + 1] != end) {
                i++
                if (Character.isDigit(chars[i]) && alias.isEmpty()) {
                    num *= 10
                    num += chars[i] - '0'
                } else {
                    alias.append(chars[i])
                }
            }
            if (i != mark && i + 1 < chars.size && chars[i + 1] == end) {
                i++
                if (alias.isNotEmpty()) {
                    val str = alias.toString()
                    builder.append((args.firstOrNull { it is Pair<*, *> && it.second == str } as? Pair<*, *>)?.first ?: "{$str}")
                } else {
                    builder.append(args.getOrNull(num) ?: "{$num}")
                }
            } else {
                i = mark
            }
        }
        if (mark == i) {
            builder.append(chars[i])
        }
        i++
    }
    return builder.toString()
}

/**
 * 解码 Unicode
 */
fun String.decodeUnicode(): String {
    var r = this
    fun process() {
        val i = r.indexOf("\\u")
        if (i != -1) {
            r = r.substring(0, i) + Integer.parseInt(r.substring(i + 2, i + 6), 16).toChar() + r.substring(i + 6)
            process()
        }
    }
    process()
    return r
}