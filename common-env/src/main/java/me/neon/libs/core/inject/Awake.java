package me.neon.libs.core.inject;

import me.neon.libs.core.LifeCycle;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * NeonLibs
 * me.neon.libs.taboolib.core.inject
 *
 * @author 老廖
 * @since 2024/3/1 10:58
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Awake {

    LifeCycle value() default LifeCycle.ENABLE;

}
