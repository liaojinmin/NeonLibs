package me.neon.libs.hook



/**
 * NeonMail-Premium
 * me.neon.mail.hook
 *
 * @author 老廖
 * @since 2024/1/17 20:01
 */
object PluginHookImpl {

    val points: HookPoints? by lazy {
        HookPoints().getImpl()
    }

    @Deprecated("弃用", replaceWith = ReplaceWith("me.neon.libs.hook.VaultService"))
    val money: HookMoney? by lazy {
        HookMoney().getImpl()
    }


}