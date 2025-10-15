package me.neon.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * NeonLibs
 * me.neon.agent
 *
 * @author 老廖
 * @since 2025/9/15 20:40
 */
public class EntityTracerAgent {

    private static File LOG_FILE = new File("entity-instrument.log");
    private static final ExecutorService logExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "SafeUnsafeListLogger");
        t.setDaemon(true);
        return t;
    });

    static {
        String ts = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        LOG_FILE = new File("entity-instrument-" + ts + ".log");
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        System.err.println("=== EntityTracerAgent premain start ===");
        log("=== premain start ===");

        new AgentBuilder.Default()
                .type(ElementMatchers.named("org.bukkit.craftbukkit.v1_12_R1.util.UnsafeList$Itr"))
                .transform((b, td, cl, m, pd) ->
                        b.method(ElementMatchers.named("next"))
                                .intercept(MethodDelegation.to(UnsafeListNext.class))
                ).installOn(inst);
       // org.bukkit.craftbukkit.v1_12_R1.util.UnsafeList
        new AgentBuilder.Default()
                .type(ElementMatchers.named("org.bukkit.craftbukkit.v1_12_R1.util.UnsafeList"))
                .transform((b, td, cl, m, pd) ->
                        b.method(ElementMatchers.named("remove"))
                                .intercept(MethodDelegation.to(UnsafeListRemove.class))
                ).installOn(inst);

        new AgentBuilder.Default()
                .type(ElementMatchers.named("org.bukkit.craftbukkit.v1_12_R1.util.UnsafeList"))
                .transform((b, td, cl, m, pd) ->
                        b.method(ElementMatchers.named("add"))
                                .intercept(MethodDelegation.to(UnsafeListAdd.class))
                ).installOn(inst);

        new AgentBuilder.Default()

                .type(ElementMatchers.nameContains("net.minecraft.server.v1_12_R1.World"))
                .transform((builder, td, cl, m, pd) ->
                        builder.constructor(ElementMatchers.any())
                                .intercept(SuperMethodCall.INSTANCE.andThen(
                                        MethodDelegation.to(WorldConstructorAdvice.class)
                                ))
                ).installOn(inst);

        System.err.println("=== EntityTracerAgent premain end ===");
        log("Agent installed. JVM pid=" + getPid());
    }

    // ================== UnsafeList Interceptors ==================
    public static class UnsafeListNext {
        public static void intercept(@Origin Method method, @This Object self) {
           // trace("UNSAFE_LIST_NEXT", method.getName(), "");
        }
    }

    public static class UnsafeListRemove {
        public static void intercept(@Origin Method method, @This Object self, @AllArguments Object[] args) {
            String type = (args != null && args.length == 1) ? args[0].getClass().getSimpleName() : "index";
            trace("UNSAFE_LIST_REMOVE", method.getName() + " type=" + type, args);
        }
    }

    public static class UnsafeListAdd {
        public static void intercept(@Origin Method method, @This Object self, @AllArguments Object[] args) {
            trace("UNSAFE_LIST_ADD", method.getName(), args);
        }
    }

    public static class WorldConstructorAdvice {

        public static void after(@This Object world) {
            if (world instanceof net.minecraft.server.v1_12_R1.World) {
               // System.out.println("is World");
                try {

                    Field entityListField = Utils.findField(world.getClass(), "entityList");
                    Field modifiersField = Field.class.getDeclaredField("modifiers");
                    modifiersField.setAccessible(true);
                    modifiersField.setInt(entityListField, entityListField.getModifiers() & ~Modifier.FINAL);
                    entityListField.set(world, new TracedEntityList((net.minecraft.server.v1_12_R1.World) world));

                    Field entityListField2 = Utils.findField(world.getClass(), "players");
                    Field modifiersField2 = Field.class.getDeclaredField("modifiers");
                    modifiersField2.setAccessible(true);
                    modifiersField2.setInt(entityListField2, modifiersField2.getModifiers() & ~Modifier.FINAL);
                    entityListField2.set(world, new TracedPlayerList((net.minecraft.server.v1_12_R1.World) world));

                    Field entityListField3 = Utils.findField(world.getClass(), "tileEntityListTick");
                    Field modifiersField3 = Field.class.getDeclaredField("modifiers");
                    modifiersField3.setAccessible(true);
                    modifiersField3.setInt(entityListField3, modifiersField3.getModifiers() & ~Modifier.FINAL);
                    entityListField3.set(world, new TracedTileEntityList((net.minecraft.server.v1_12_R1.World) world));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean isBukkitMainThread() {
        Thread t = Thread.currentThread();
        String name = t.getName();
        return name != null && name.equalsIgnoreCase("Server thread");
    }

    public static void trace(String tag, Object origin, Object... args) {
        try {
            Thread t = Thread.currentThread();
            String tname = t.getName();
            boolean plausibleMain = tname != null && tname.toLowerCase().contains("server");
            // Collect stack
            StackTraceElement[] st = Thread.currentThread().getStackTrace();
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(tag).append("] thread=").append(tname)
                    .append(" plausibleMain=").append(plausibleMain)
                    .append(" origin=").append(origin)
                    .append(" pid=").append(getPid())
                    .append(" args=").append(Arrays.toString(args))
                    .append("\n");
            // find first non-minecraft frame (probable plugin caller)
            String probableCaller = null;
            for (StackTraceElement e : st) {
                String cn = e.getClassName();
                if (cn.startsWith("net.minecraft") || cn.startsWith("org.bukkit") || cn.startsWith("java.") || cn.startsWith("sun.") || cn.startsWith("com.sun.") || cn.startsWith("agent.")) {
                    continue;
                }
                probableCaller = e.toString();
                break;
            }
            if (probableCaller != null) {
                sb.append(" probableCaller=").append(probableCaller).append("\n");
            } else {
                sb.append(" probableCaller=NOT_FOUND\n");
            }
            sb.append(" full stack:\n");
            int max = Math.min(st.length, 40);
            for (int i = 0; i < max; i++) {
                sb.append("   at ").append(st[i].toString()).append("\n");
            }
            writeLog(sb.toString());
        } catch (Throwable ignored) {}
    }

    private static synchronized void writeLog(String s) {
        logExecutor.submit(() -> {
            try (FileWriter fw = new FileWriter(LOG_FILE, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                pw.println("----- " + ts + " -----");
                pw.println(s);
                pw.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void log(String s) {
        logExecutor.submit(() -> {
            try (FileWriter fw = new FileWriter(LOG_FILE, true); PrintWriter pw = new PrintWriter(fw)) {
                String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                pw.println(ts + " " + s);
                pw.flush();
            } catch (Exception ignored) {}
        });

    }

    private static String getPid() {
        try {
            String jvm = ManagementFactory.getRuntimeMXBean().getName();
            if (jvm != null && jvm.contains("@")) return jvm.split("@")[0];
        } catch (Throwable ignored) {}
        return "?";
    }
}
