package me.neon.agent;

/**
 * NeonLibs
 * me.neon.agent
 *
 * @author 老廖
 * @since 2025/8/31 12:08
 */
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class FlyInterceptor {

    /**
     * 拦截 setFlying / setAllowFlight
     * @param value 方法参数
     * @param self 被拦截对象（CraftPlayer）
     * @param method 被拦截的方法
     */
    public static void intercept(boolean value,
                                 @This Object self,
                                 @Origin Method method,
                                 @SuperCall Callable<Void> zuper
    ) {
        if (value) {
            // 阻止飞行：直接返回，不调用原方法
            System.out.println("[FlyAgent] 阻止飞行: " + method.getName() +
                    " 参数=" + value + " 玩家=" + self);

            // 打印调用堆栈，可选
            new Throwable("调用堆栈").printStackTrace();
        }

        try {
            // 调用原方法，继续执行原本逻辑
            zuper.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
