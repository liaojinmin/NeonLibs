package me.neon.libs.core;

/**
 * TabooLib
 * taboolib.common.LifeCycle
 *
 * @author sky
 * @since 2021/6/28 10:28 下午
 */
public enum LifeCycle {

    /**
     * 未启动
     */
    NONE,

    /**
     * 插件启用
     **/
    ENABLE,

    /**
     * 完全启动
     **/
    ACTIVE,

    /**
     * 插件卸载时
     **/
    DISABLE
}
