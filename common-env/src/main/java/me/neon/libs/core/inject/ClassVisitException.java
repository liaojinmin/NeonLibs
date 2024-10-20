package me.neon.libs.core.inject;

import org.tabooproject.reflex.ClassField;
import org.tabooproject.reflex.ClassMethod;

/**
 * TabooLib
 * taboolib.common.inject.ClassVisitException
 *
 * @author 坏黑
 * @since 2022/8/5 20:27
 */
public class ClassVisitException extends RuntimeException {

    public ClassVisitException(Class<?> clazz, Throwable cause) {
        super(clazz.toString(), cause);
    }

    public ClassVisitException(Class<?> clazz, VisitorGroup group, Throwable cause) {
        super(clazz + ": " + group, cause);
    }

    public ClassVisitException(Class<?> clazz, VisitorGroup group, ClassField field, Throwable cause) {
        super(clazz + "#" + field.getName() + ": " + group, cause);
    }

    public ClassVisitException(Class<?> clazz, VisitorGroup group, ClassMethod method, Throwable cause) {
        super(clazz + "#" + method.getName() + ": " + group, cause);
    }
}
