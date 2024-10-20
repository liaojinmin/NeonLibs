package demo;

/**
 * NeonLibs
 * demo
 *
 * @author 老廖
 * @since 2024/7/10 1:57
 */
public class MyClass {

    public static void targetStaticMethod() {
        System.out.println("Original Static Method Invoked!");
    }

    public static void main(String[] args) {
        MyClass.targetStaticMethod();

    }
}
