package me.neon.libs.hook



/**
 * NeonMail-Premium
 * me.neon.mail.hook
 *
 * @author 老廖
 * @since 2024/1/17 20:01
 */
object PluginHoolImpl {

    val points: HookPoints? by lazy {
        HookPoints().getImpl()
    }

    val money: HookMoney? by lazy {
        HookMoney().getImpl()
    }


}