package com.github.mkram17.bazaarutils.data.storage;

import com.github.mkram17.bazaarutils.utils.Util;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class FolderStorage<T> {

    private final String folder;
    private final Type dataType;
    private final Map<String, DataStorage<T>> storages = new LinkedHashMap<>();
    private final Path folderPath;

    public FolderStorage(String folder, Type dataType) {
        this.folder = folder;
        this.dataType = dataType;
        this.folderPath = DataStorage.DEFAULT_PATH.resolve(folder);
        load();
    }

    public void add(T value) { set(String.valueOf(value.hashCode()), value); }

    public void set(String id, T value) {
        DataStorage<T> storage = storages.computeIfAbsent(id,
                k -> new DataStorage<>(() -> value, folder + "/" + id, dataType));
        storage.set(value);
        storage.save();
    }

    @Nullable
    public T get(String id) {
        DataStorage<T> s = storages.get(id);
        return s != null ? s.get() : null;
    }

    public void remove(String id) {
        DataStorage<T> s = storages.remove(id);
        if (s != null) s.delete();
    }

    public Map<String, T> getAll() {
        Map<String, T> result = new LinkedHashMap<>();
        storages.forEach((k, v) -> result.put(k, v.get()));
        return Collections.unmodifiableMap(result);
    }

    public boolean contains(String id) { return storages.containsKey(id); }

    public void refresh() { storages.clear(); load(); }

    private void load() {
        try { Files.createDirectories(folderPath); }
        catch (IOException e) { Util.logError("Failed to create folder: " + folderPath, e); return; }

        try (Stream<Path> files = Files.list(folderPath)) {
            files.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".json"))
                    .forEach(file -> {
                        String id = file.getFileName().toString().replace(".json", "");
                        try {
                            storages.put(id, new DataStorage<>(
                                    () -> { throw new IllegalStateException("No default for folder entry: " + id); },
                                    folder + "/" + id, dataType));
                        } catch (Exception e) {
                            Util.logError("Failed to load folder entry: " + file, e);
                        }
                    });
        } catch (IOException e) {
            Util.logError("Failed to list folder: " + folderPath, e);
        }
    }
}