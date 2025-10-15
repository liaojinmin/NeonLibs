package me.neon.libs.taboolib.configuration.annotation

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Config(
    val value: String = "config.yml",
    val migrate: Boolean = false,
    val autoReload: Boolean = false,
    val concurrent: Boolean = true,
)