package me.neon.libs.core.demo;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.Bukkit;

import java.util.concurrent.Callable;

/**
 * NeonLibs
 * me.neon.libs.core.demo
 *
 * @author 老廖
 * @since 2025/8/30 17:51
 */
public class BytebuddyLoader {


    public static void loader() {

        try {
            // 安装 ByteBuddy Agent
            ByteBuddyAgent.install();

            // 拦截 setAllowFlight
            Class<?> clazz = Class.forName("org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer");
            new ByteBuddy()
                    .redefine(clazz)
                    .method(ElementMatchers.named("setAllowFlight"))
                    .intercept(MethodDelegation.to(AllowFlightHandler.class))
                    .make()
                    .load(clazz.getClassLoader(),
                            ClassReloadingStrategy.fromInstalledAgent());

            // 拦截 setFlying
            new ByteBuddy()
                    .redefine(clazz)
                    .method(ElementMatchers.named("setFlying"))
                    .intercept(MethodDelegation.to(FlyingHandler.class))
                    .make()
                    .load(clazz.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

            Bukkit.getLogger().info("✅ FlightHook 已注入 setAllowFlight() 与 setFlying()");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class AllowFlightHandler {

        public static void intercept(boolean flying) throws Exception {
            System.out.println("⚡ setAllowFlight 被调用: -> " + flying);
            new Throwable().printStackTrace();
            // original.call();
        }

        public static void intercept(@This Object player,
                                     @AllArguments Object[] args,
                                     @SuperCall Callable<Void> original
        ) throws Exception {
            boolean flying = (boolean) args[0];
            // 反射获取玩家名字
            String name = (String) player.getClass().getMethod("getName").invoke(player);
            System.out.println("⚡ setAllowFlight 被调用: " + name + " -> " + flying);
            new Throwable().printStackTrace();
           // original.call();
        }
    }

    public static class FlyingHandler {

        public void intercept(
                @This Object player,
                @AllArguments Object[] args,
                @SuperCall Callable<Void> original
        ) throws Exception {
            boolean flying = (boolean) args[0];
            String name = (String) player.getClass().getMethod("getName").invoke(player);
            System.out.println("⚡ setFlying 被调用: " + name + " -> " + flying);
            new Throwable().printStackTrace();
            original.call();
        }
    }
}
