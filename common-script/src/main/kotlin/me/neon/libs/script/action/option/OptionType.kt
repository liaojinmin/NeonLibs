package me.neon.libs.script.action.option

/**
 * NeonFlash
 * me.neon.flash.api.action.option
 *
 * @author 老廖
 * @since 2024/3/20 14:08
 */
enum class OptionType(val regex: Regex, val group: Int) {

    CHANCE("[{<](chance|rate|rand(om)?)[=:] ?([0-9.]+)[}>]", 3),

    CONDITION("[{<](condition|requirement)[=:] ?(.+)[}>]", 2);

    constructor(regex: String, group: Int) : this("(?i)$regex".toRegex(), group)


}