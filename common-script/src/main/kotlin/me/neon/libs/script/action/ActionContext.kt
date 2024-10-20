package me.neon.libs.script.action

/**
 * NeonFlash
 * me.neon.flash.api.action
 *
 * @author 老廖
 * @since 2024/3/20 13:33
 */
class ActionContext: HashMap<String, Any> {

    constructor(defContent: Any? = null) : super() {
        defContent ?: return
        stringContent(defContent)
    }

    constructor(key: String, any: Any) {
        this[key] = any
    }

    fun stringContent(value: Any? = null): String {
        if (value != null) {
            this["content"] = value
        }
        return this["content"]?.toString()!!
    }

    override fun toString(): String {
        return runCatching { stringContent() }.getOrNull() ?: super.toString()
    }
}