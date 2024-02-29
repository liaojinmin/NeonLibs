package me.neon.libs.utils

import me.clip.placeholderapi.PlaceholderAPI
import me.neon.libs.NeonLibsLoader
import me.neon.libs.taboolib.chat.HexColor.colored
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * NeonMail
 * me.neon.mail.libs.utils
 *
 * @author 老廖
 * @since 2024/2/14 15:50
 */

object StringUtils {
    val expiryRegex: Regex by lazy { Regex("\\d+?(?i)(d|h|m|s|天|时|分|秒)\\s?") }
}

/**
 * 取字符串的数字签名
 *
 * @param algorithm 算法类型（可使用：md5, sha-1, sha-256 等）
 * @return 数字签名
 */
fun String.digest(algorithm: String): String {
    val digest = MessageDigest.getInstance(algorithm)
    digest.update(toByteArray(StandardCharsets.UTF_8))
    return BigInteger(1, digest.digest()).toString(16)
}

fun parseStringTimerToLong(time: String): Long {
    if (time.isEmpty() || time == "-1") return -1
    var timer: Long = 0
    StringUtils.expiryRegex.findAll(time).forEach {
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

fun String.replaceWithOrder(vararg args: Any): String {
    if (args.isEmpty() || isEmpty()) {
        return this
    }
    val chars = toCharArray()
    val builder = StringBuilder(length)
    var i = 0
    while (i < chars.size) {
        val mark = i
        if (chars[i] == '{') {
            var num = 0
            val alias = StringBuilder()
            while (i + 1 < chars.size && chars[i + 1] != '}') {
                i++
                if (Character.isDigit(chars[i]) && alias.isEmpty()) {
                    num *= 10
                    num += chars[i] - '0'
                } else {
                    alias.append(chars[i])
                }
            }
            if (i != mark && i + 1 < chars.size && chars[i + 1] == '}') {
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

fun Any.asList(): List<String> {
    return when (this) {
        is Collection<*> -> map { it.toString() }
        is Array<*> -> map { it.toString() }
        else -> listOf(toString())
    }
}

fun <T> subList(list: List<T>, start: Int = 0, end: Int = list.size): List<T> {
    return list.filterIndexed { index, _ -> index in start until end }
}

fun info(text: String) {
    Bukkit.getConsoleSender().sendMessage("[NeonLibs] ${text.colored()}")
}

fun log(text: String) {
    NeonLibsLoader.print(text)
}

fun warning(text: String) {
    NeonLibsLoader.warning(text)
}





