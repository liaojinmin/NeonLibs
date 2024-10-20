package me.neon.libs.carrier

/**
 * NeonDesire
 * me.neon.desire.feature.task
 *
 * @author 老廖
 * @since 2024/4/27 13:57
 */
data class MetaDataString(
    override var value: String,
    override var timer: Long = -1L
): MetaDataValue<String> {

    override fun isTimerOut(): Boolean {
        return timer > 0 && timer <= System.currentTimeMillis()
    }

    override fun asBoolean(def: Boolean): Boolean {
        return if (value.equals("true", true) || value.equals("false", true)) {
            value.toBoolean()
        } else def
    }

    override fun asInt(): Int {
        return value.toIntOrNull() ?: 0
    }

    override fun asDouble(): Double {
        return value.toDoubleOrNull() ?: 0.0
    }

    override fun toString(): String {
        return value
    }

}
