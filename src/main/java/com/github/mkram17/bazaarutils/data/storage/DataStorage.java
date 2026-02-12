package com.github.mkram17.bazaarutils.data.storage;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.events.util.EventPriorities;
import com.github.mkram17.bazaarutils.misc.autoregistration.RunOnInit;
import com.github.mkram17.bazaarutils.utils.Util;
import com.google.gson.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class DataStorage<T> {

    private static final Set<DataStorage<?>> REQUIRES_SAVE = ConcurrentHashMap.newKeySet();
    static final Path DEFAULT_PATH = FabricLoader.getInstance().getConfigDir().resolve(BazaarUtils.MODID).resolve("data");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static int tickCounter = 0;

    @RunOnInit(priority = EventPriorities.HIGH)
    public static void registerTickListener() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (++tickCounter >= 100) {
                tickCounter = 0;
                if (REQUIRES_SAVE.isEmpty()) return;
                DataStorage<?>[] toSave = REQUIRES_SAVE.toArray(new DataStorage<?>[0]);
                REQUIRES_SAVE.clear();
                CompletableFuture.runAsync(() -> {
                    for (DataStorage<?> s : toSave) s.saveToSystem();
                });
            }
        });
    }

    public static void flushAll() {
        DataStorage<?>[] toSave = REQUIRES_SAVE.toArray(new DataStorage<?>[0]);
        REQUIRES_SAVE.clear();
        for (DataStorage<?> s : toSave) s.saveToSystem();
    }

    private final int version;
    private final Type dataType;
    private final Path path;
    private T data;

    public DataStorage(int version, Supplier<T> defaultData, String fileName, Type dataType) {
        this.version = version;
        this.dataType = dataType;
        this.path = DEFAULT_PATH.resolve(fileName + ".json");
        this.data = load(defaultData);
    }

    public DataStorage(Supplier<T> defaultData, String fileName, Type dataType) {
        this(0, defaultData, fileName, dataType);
    }

    public T get() { return data; }
    public void set(T newData) { this.data = newData; }
    public void save() { REQUIRES_SAVE.add(this); }

    public void delete() {
        try { Files.deleteIfExists(path); }
        catch (IOException e) { Util.logError("Failed to delete " + path, e); }
    }

    private T load(Supplier<T> defaultData) {
        if (!Files.exists(path)) {
            try { Files.createDirectories(path.getParent()); }
            catch (IOException e) { Util.logError("Failed to create data directory", e); }
            return defaultData.get();
        }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            int fileVersion = root.get("@bazaarutils:version").getAsInt();
            if (fileVersion != this.version) {
                Util.logMessage("Version mismatch for " + path.getFileName() + ", using defaults.");
                return defaultData.get();
            }
            T loaded = GSON.fromJson(root.get("@bazaarutils:data"), dataType);
            return loaded != null ? loaded : defaultData.get();
        } catch (Exception e) {
            Util.logError("Failed to load " + path + ", using defaults.", e);
            return defaultData.get();
        }
    }

    private void saveToSystem() {
        try {
            Files.createDirectories(path.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("@bazaarutils:version", version);
            root.add("@bazaarutils:data", GSON.toJsonTree(data, dataType));
            Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (Exception e) {
            Util.logError("Failed to save " + path, e);
        }
    }
}