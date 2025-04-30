package com.github.mkram17.bazaarutils.misc;

import com.github.mkram17.bazaarutils.Utils.Util;
import com.github.mkram17.bazaarutils.Utils.Util.notificationTypes;
import com.google.gson.*;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModCompatibilityHelper {
    private static final String REI_MOD_ID = "roughlyenoughitems";
    private static final String SKYBLOCKER_MOD_ID = "skyblocker";
    private static final String REI_CONFIG_FILENAME = "roughlyenoughitems/config.json5";
    private static final String REI_CONFIG_SECTION = "appearance";
    private static final String REI_CONFIG_FIELD = "horizontalEntriesBoundariesColumns";
    private static final int HORIZONTALENTRIESBOUNDARIESCOLUMS_VALUE = 16;

    private static final Gson GSON_WRITER = new GsonBuilder().setPrettyPrinting().create();

    public void initializePatches(){
        if (FabricLoader.getInstance().isModLoaded(REI_MOD_ID)) {
            Util.notifyAll("Bazaar Utils: REI detected. Attempting to modify REI config.");
            modifyReiConfigWithGson();
        }
        tryInitializeSkyblockerIntegration();
    }

    private void modifyReiConfigWithGson() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path reiConfigFile = configDir.resolve(REI_CONFIG_FILENAME);

        if (!Files.exists(reiConfigFile)) {
            Util.notifyAll("Bazaar Utils: Could not find REI config file at: " + reiConfigFile, notificationTypes.ERROR);
            return;
        }

        JsonObject rootObject = null;

        try (BufferedReader reader = Files.newBufferedReader(reiConfigFile, StandardCharsets.UTF_8)) {
            JsonElement rootElement = JsonParser.parseReader(reader);

            if (rootElement.isJsonObject()) {
                rootObject = rootElement.getAsJsonObject();
            } else {
                Util.notifyAll("Bazaar Utils: REI config root is not a JSON object: " + reiConfigFile, notificationTypes.ERROR);
                return;
            }

        } catch (JsonSyntaxException e) {
            Util.notifyAll("Bazaar Utils: Failed to parse REI config file (likely due to non-standard JSON5 features like comments that Gson couldn't handle, or actual syntax errors): " + reiConfigFile, notificationTypes.ERROR);
            return;
        } catch (IOException e) {
            Util.notifyAll("Bazaar Utils: Failed to read REI config file: " + reiConfigFile, notificationTypes.ERROR);
            return;
        }

        try {
            if (rootObject != null && rootObject.has(REI_CONFIG_SECTION) && rootObject.get(REI_CONFIG_SECTION).isJsonObject()) {
                JsonObject appearanceObject = rootObject.getAsJsonObject(REI_CONFIG_SECTION);

                if (appearanceObject.has(REI_CONFIG_FIELD)) {
                    JsonElement currentValue = appearanceObject.get(REI_CONFIG_FIELD);
                    Util.notifyAll("Bazaar Utils: Current REI value for '" + REI_CONFIG_SECTION + "." + REI_CONFIG_FIELD + "': " + currentValue, notificationTypes.ERROR);
                } else {
                    Util.notifyAll("Bazaar Utils: Key '" + REI_CONFIG_SECTION + "." + REI_CONFIG_FIELD + "' not found in REI config.", notificationTypes.ERROR);
                }

                appearanceObject.addProperty(REI_CONFIG_FIELD, HORIZONTALENTRIESBOUNDARIESCOLUMS_VALUE);
                Util.notifyAll("Bazaar Utils: Set REI value for '" + REI_CONFIG_SECTION + "." + REI_CONFIG_FIELD + "' to: " + HORIZONTALENTRIESBOUNDARIESCOLUMS_VALUE, notificationTypes.ERROR);

            } else {
                Util.notifyAll("Bazaar Utils: REI config structure unexpected. Missing '" + REI_CONFIG_SECTION + "' object.", notificationTypes.ERROR);
                return;
            }
        } catch (Exception e) {
            Util.notifyAll("Bazaar Utils: Error modifying the JSON structure in memory.", notificationTypes.ERROR);
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(reiConfigFile, StandardCharsets.UTF_8)) {
            GSON_WRITER.toJson(rootObject, writer);
            Util.notifyAll("Bazaar Utils: Successfully saved modified REI config (comments removed): " + reiConfigFile, notificationTypes.ERROR);
        } catch (IOException e) {
            Util.notifyAll("Bazaar Utils: Failed to write modified REI config file: " + reiConfigFile, notificationTypes.ERROR);
        }
    }


    @Getter
    private boolean skyblockerDetected = false;
    private Class<?> searchOverManagerClass = null;
    private Method updateSearchMethod = null;
    private Method pushSearchMethod = null;
    private Field isAuctionField = null;
    private Field isCommandField = null;

    public void tryInitializeSkyblockerIntegration() {
        if (FabricLoader.getInstance().isModLoaded(SKYBLOCKER_MOD_ID)) { 
            try {
                searchOverManagerClass = Class.forName("de.hysky.skyblocker.skyblock.searchoverlay.SearchOverManager");

                isAuctionField = searchOverManagerClass.getDeclaredField("isAuction");
                isAuctionField.setAccessible(true);

                isCommandField = searchOverManagerClass.getDeclaredField("isCommand");
                isCommandField.setAccessible(true);

                updateSearchMethod = searchOverManagerClass.getDeclaredMethod("updateSearch", String.class);
                updateSearchMethod.setAccessible(true);

                pushSearchMethod = searchOverManagerClass.getDeclaredMethod("pushSearch");
                pushSearchMethod.setAccessible(true);

                skyblockerDetected = true;
                System.out.println("Bazaar Utils: Skyblocker detected! Enabling integration.");

            } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | SecurityException e) {
                System.err.println("Bazaar Utils: Skyblocker detected, but failed to integrate (likely version mismatch or internal changes).");
                e.printStackTrace();
                skyblockerDetected = false;
            }
        } else {
            System.out.println("Bazaar Utils: Skyblocker not found. Using fallback search.");
            skyblockerDetected = false;
        }
    }

    public boolean skyblockerSearchBazaar(String itemName) {
        if (skyblockerDetected && searchOverManagerClass != null) {
            try {
                isAuctionField.setBoolean(null, false);
                isCommandField.setBoolean(null, true);
                updateSearchMethod.invoke(null, itemName);
                pushSearchMethod.invoke(null);
                return true;

            } catch (Exception e) {
                System.err.println("Bazaar Utils: Error calling Skyblocker via reflection. Falling back.");
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
    
}
