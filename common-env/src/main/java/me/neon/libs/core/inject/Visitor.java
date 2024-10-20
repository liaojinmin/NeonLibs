package me.neon.libs.core.inject;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * NeonLibs
 * me.neon.libs.taboolib.core.inject
 * 使用此注解标记的类，会尝试将其解释为 {@link ClassVisitor}
 *
 * @author 老廖
 * @since 2024/3/1 10:55
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Visitor {
}
