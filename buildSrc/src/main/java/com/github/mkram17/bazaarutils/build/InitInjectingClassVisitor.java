package com.github.mkram17.bazaarutils.build;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

public class InitInjectingClassVisitor extends ClassVisitor {
    private final List<BuildtimeInjectionTask.MethodReference> methodSignatures;
    private final String targetMethodName;
    private final String targetMethodDesc;

    public InitInjectingClassVisitor(ClassVisitor classVisitor, List<BuildtimeInjectionTask.MethodReference> methodSignatures, String targetMethodName, String targetMethodDesc) {
        super(Opcodes.ASM9, classVisitor);
        this.methodSignatures = methodSignatures;
        this.targetMethodName = targetMethodName;
        this.targetMethodDesc = targetMethodDesc;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor originalMethodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);

        if (name.equals(targetMethodName) && descriptor.equals(targetMethodDesc)) {
            return new MethodVisitor(Opcodes.ASM9, originalMethodVisitor) {
                @Override
                public void visitInsn(int opcode) {
                    // Inject calls just before the method returns. For a void method, this is RETURN.
                    if (opcode == Opcodes.RETURN) {
                        for (BuildtimeInjectionTask.MethodReference methodCall : methodSignatures) {
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, methodCall.className(), methodCall.methodName(), methodCall.descriptor(), false);
                        }
                    }
                    super.visitInsn(opcode);
                }
            };
        }

        return originalMethodVisitor;
    }
}