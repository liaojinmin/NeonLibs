package me.neon.agent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import java.lang.instrument.Instrumentation;


/**
 * NeonLibs
 * me.neon.agent
 *
 * @author 老廖
 * @since 2025/8/31 12:08
 */
public class FlyAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[FlyAgent] 初始化");
        new AgentBuilder.Default()
                // 1.12.2 对应 v1_12_R1
                .type(ElementMatchers.named("org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer"))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.method(ElementMatchers.named("setFlying")
                                        .or(ElementMatchers.named("setAllowFlight")))
                                .intercept(MethodDelegation.to(FlyInterceptor.class))
                )
                .installOn(inst);

        System.out.println("[FlyAgent] 已挂载，正在拦截 CraftPlayer#setFlying / setAllowFlight");

        inst.addTransformer(new FlyTransformer(), true);

        /*
        try {
            Class<?> clazz = Class.forName("net.minecraft.server.v1_12_R1.PlayerAbilities");
            inst.retransformClasses(clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }

         */
    }
}

