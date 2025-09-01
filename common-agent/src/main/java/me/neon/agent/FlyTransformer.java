package me.neon.agent;


import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;


/**
 * NeonLibs
 * me.neon.agent
 *
 * @author 老廖
 * @since 2025/8/31 14:39
 */
public class FlyTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
      //  if (className.startsWith("net/minecraft/server")) {
     //       System.out.println("class: " + className);
     //   }
        if (!"net/minecraft/server/v1_12_R1/PlayerAbilities".equals(className)) {
            return null;
        }
      //  System.out.println("注入 net/minecraft/server/v1_12_R1/PlayerAbilities");

        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new ClassVisitor(ASM6, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                return new MethodVisitor(ASM6, mv) {
                    @Override
                    public void visitFieldInsn(int opcode, String owner, String fieldName, String fieldDesc) {
                     //   System.out.println("名称 "+fieldName);
                        if (opcode == PUTFIELD && "isFlying".equalsIgnoreCase(fieldName)) {
                            // 替换字段写入为调用 FlyHook
                         //   mv.visitVarInsn(ALOAD, 0); // this
                         //   mv.visitVarInsn(ILOAD, 1); // boolean value
                            mv.visitInsn(DUP2); // 复制栈上 this + value，不消费原来的栈
                            mv.visitMethodInsn(INVOKESTATIC,
                                    "me/neon/agent/FlyHook",
                                    "onFlyingSet",
                                    "(Lnet/minecraft/server/v1_12_R1/PlayerAbilities;Z)V",
                                    false);
                            super.visitFieldInsn(opcode, owner, fieldName, fieldDesc);
                        } else {
                            super.visitFieldInsn(opcode, owner, fieldName, fieldDesc);
                        }
                    }
                };
            }
        };

        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

}

