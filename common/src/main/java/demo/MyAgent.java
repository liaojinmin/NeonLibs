package demo;

import java.lang.instrument.Instrumentation;

/**
 * NeonLibs
 * demo
 *
 * @author 老廖
 * @since 2024/7/10 1:57
 */
public class MyAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new MyClassTransformer());
    }
}
