package me.neon.libs.taboolib.event

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubscribeEvent(
    val priority: EventPriority = EventPriority.NORMAL,
    val ignoreCancelled: Boolean = false,
    // 用于 OptionalEvent
    val bind: String = ""
)