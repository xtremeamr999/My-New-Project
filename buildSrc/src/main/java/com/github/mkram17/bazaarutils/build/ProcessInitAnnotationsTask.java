package com.github.mkram17.bazaarutils.build;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class ProcessInitAnnotationsTask extends DefaultTask {

    @InputDirectory
    public abstract DirectoryProperty getClassesDir();

    @TaskAction
    public void execute() {
        getLogger().info("Starting injection of @RunOnInit methods.");
        long start = System.currentTimeMillis();

        File classesDir = getClassesDir().get().getAsFile();
        Map<MethodReference, Integer> methodSignatures = new HashMap<>();

        // 1. Find all methods with the @RunOnInit annotation
        findInitMethods(classesDir, methodSignatures);

        // 2. Sort the methods by their priority.
        List<MethodReference> sortedMethodSignatures = methodSignatures.entrySet()
                .stream()
                .sorted(Map.Entry.<MethodReference, Integer>comparingByValue().thenComparing(entry -> entry.getKey().className()))
                .map(Map.Entry::getKey)
                .toList();
        
        getLogger().info("Found {} methods to inject.", sortedMethodSignatures.size());

        // 3. Inject calls to the @RunOnInit annotated methods in the BazaarUtils class
        injectInitCalls(classesDir, sortedMethodSignatures);

        getLogger().lifecycle("Injecting @RunOnInit methods took: {}ms", (System.currentTimeMillis() - start));
    }

    private void findInitMethods(File classesDir, Map<MethodReference, Integer> methodSignatures) {
        forEachClass(classesDir, inputStream -> {
            try {
                ClassReader classReader = new ClassReader(inputStream);
                classReader.accept(new InitReadingClassVisitor(classReader, methodSignatures), ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read class for annotation processing", e);
            }
        });
    }

    private void injectInitCalls(File classesDir, List<MethodReference> methodSignatures) {
        String mainClassName = "com/github/mkram17/bazaarutils/BazaarUtils.class";
        Path mainClassFile = Objects.requireNonNull(findClass(classesDir, mainClassName), "BazaarUtils class wasn't found").toPath();

        try (InputStream inputStream = Files.newInputStream(mainClassFile)) {
            ClassReader classReader = new ClassReader(inputStream);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            classReader.accept(new InitInjectingClassVisitor(classWriter, methodSignatures, "onInitializeClient", "()V"), 0);
            try (OutputStream outputStream = Files.newOutputStream(mainClassFile)) {
                outputStream.write(classWriter.toByteArray());
            }
        } catch (Exception e) {
            getLogger().error("Failed to inject init calls into {}", mainClassFile, e);
        }
    }

    private void forEachClass(File directory, final Consumer<InputStream> consumer) {
        try {
            Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                    if (!path.toString().endsWith(".class")) return FileVisitResult.CONTINUE;
                    try (InputStream inputStream = Files.newInputStream(path)) {
                        consumer.accept(inputStream);
                    } catch (IOException e) {
                        getLogger().error("Failed to run consumer on class {}", path, e);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            getLogger().error("Failed to walk class directory {}", directory, e);
        }
    }

    private File findClass(File directory, String className) {
        if (!className.endsWith(".class")) className += ".class";
        Path classPath = directory.toPath().resolve(className);
        return Files.exists(classPath) ? classPath.toFile() : null;
    }

    /**
     * @param className  the class name (e.g. com/github/mkram17/bazaarutils/utils/GUIUtils)
     * @param methodName the method's name (e.g. init)
     * @param descriptor the method's descriptor (e.g. ()V)
     * @param itf        whether the target class is an {@code interface} or not
     */
    public record MethodReference(String className, String methodName, String descriptor, boolean itf) {}
}