package me.neon.libs.core.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * NeonLibs
 * me.neon.libs.taboolib.core.inject
 * 如果有此注解，在获取实例时，将进行实例化
 *
 * @author 老廖
 * @since 2024/3/1 14:11
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Instance {
}
