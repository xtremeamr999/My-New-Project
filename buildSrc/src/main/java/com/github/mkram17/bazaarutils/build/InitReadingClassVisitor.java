package com.github.mkram17.bazaarutils.build;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;

import java.util.Map;

public class InitReadingClassVisitor extends ClassVisitor {
    private final Map<ProcessInitAnnotationsTask.MethodReference, Integer> methodSignatures;
    private final ClassReader classReader;

    public InitReadingClassVisitor(ClassReader classReader, Map<ProcessInitAnnotationsTask.MethodReference, Integer> methodSignatures) {
        super(Opcodes.ASM9);
        this.classReader = classReader;
        this.methodSignatures = methodSignatures;
    }

    @Override
    public MethodVisitor visitMethod(int access, String methodName, String descriptor, String signature, String[] exceptions) {
        return new MethodVisitor(Opcodes.ASM9) {
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (!desc.equals("Lcom/github/mkram17/bazaarutils/misc/entrypoints/RunOnInit;")) return super.visitAnnotation(desc, visible);

                return new InitAnnotationVisitor(methodSignatures, getMethodCall());
            }

            private @NotNull ProcessInitAnnotationsTask.MethodReference getMethodCall() {
                String className = classReader.getClassName();
                String methodCallString = className + "." + methodName;
                if ((access & Opcodes.ACC_PUBLIC) == 0) throw new IllegalStateException(methodCallString + ": Initializer methods must be public");
                if ((access & Opcodes.ACC_STATIC) == 0) throw new IllegalStateException(methodCallString + ": Initializer methods must be static");
                if (!descriptor.equals("()V")) throw new IllegalStateException(methodCallString + ": Initializer methods must have no args and a void return type");

                boolean itf = (classReader.getAccess() & Opcodes.ACC_INTERFACE) != 0;

                return new ProcessInitAnnotationsTask.MethodReference(className, methodName, descriptor, itf);
            }
        };
    }

    static class InitAnnotationVisitor extends AnnotationVisitor {
        private final Map<ProcessInitAnnotationsTask.MethodReference, Integer> methodSignatures;
        private final ProcessInitAnnotationsTask.MethodReference methodCall;

        protected InitAnnotationVisitor(Map<ProcessInitAnnotationsTask.MethodReference, Integer> methodSignatures, ProcessInitAnnotationsTask.MethodReference methodCall) {
            super(Opcodes.ASM9);
            this.methodSignatures = methodSignatures;
            this.methodCall = methodCall;
        }

        @Override
        public void visitEnd() {
            // If no priority was explicitly set, default to NORMAL (2)
            methodSignatures.putIfAbsent(methodCall, 2);
            super.visitEnd();
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            if (name.equals("priority")) {
                int priorityValue = switch (value) {
                    case "LOWEST" -> 0;
                    case "LOW" -> 1;
                    case "NORMAL" -> 2;
                    case "HIGH" -> 3;
                    case "HIGHEST" -> 4;
                    default -> 2; // Default to normal if unknown
                };
                methodSignatures.put(methodCall, priorityValue);
            }
            super.visitEnum(name, descriptor, value);
        }
    }
}