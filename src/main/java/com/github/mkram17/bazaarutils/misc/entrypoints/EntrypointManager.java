package com.github.mkram17.bazaarutils.misc.entrypoints;

import com.github.mkram17.bazaarutils.utils.Util;
import io.github.classgraph.AnnotationEnumValue;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;

public class EntrypointManager {

    private static final String RUN_ON_INIT_ANNOTATION = RunOnInit.class.getName();

    public static void registerInitMethods() {
        String basePackage = "com.github.mkram17.bazaarutils";

        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages(basePackage)
                .scan()) {

            List<Method> initializers = scanResult.getClassesWithMethodAnnotation(RUN_ON_INIT_ANNOTATION)
                    .stream()
                    .flatMap(classInfo -> classInfo.getMethodInfo().stream())
                    .filter(methodInfo -> methodInfo.hasAnnotation(RUN_ON_INIT_ANNOTATION))
                    .peek(EntrypointManager::validateMethod) // Validate each method before processing
                    .sorted(Comparator.comparingInt(EntrypointManager::getPriority))
                    .map(MethodInfo::loadClassAndGetMethod) // Convert to java.lang.reflect.Method
                    .toList();

            for (Method method : initializers) {
                try {
                    // Invoke the static method (pass null for the instance)
                    method.invoke(null);
                } catch (Exception e) {
                    Util.notifyError("Failed to run initializer: " + method.getName(), e);
                }
            }
        }
    }

    private static int getPriority(MethodInfo methodInfo) {
        Object priorityValue = methodInfo.getAnnotationInfo(RUN_ON_INIT_ANNOTATION)
                .getParameterValues().getValue("priority");
        String enumConstantName = ((AnnotationEnumValue) priorityValue).getValueName();
        RunOnInit.EVENT_PRIORITIES priority = RunOnInit.EVENT_PRIORITIES.valueOf(enumConstantName);
        return priority.getValue();
    }

    private static void validateMethod(MethodInfo methodInfo) {
        if (!methodInfo.isStatic()) {
            throw new RuntimeException("Initializer method must be static: " + methodInfo.getName());
        }
        if (!methodInfo.isPublic()) {
            throw new RuntimeException("Initializer method must be public: " + methodInfo.getName());
        }
        if (methodInfo.getParameterInfo().length > 0) {
            throw new RuntimeException("Initializer method must have no parameters: " + methodInfo.getName());
        }
    }
}