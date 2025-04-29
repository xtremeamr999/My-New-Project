package com.github.mkram17.bazaarutils.misc;

import com.github.mkram17.bazaarutils.Utils.Util;
import com.github.mkram17.bazaarutils.Utils.Util.notificationTypes;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModCompatabilityHelper {
    private static final String REI_MOD_ID = "roughlyenoughitems";
    private static final String REI_CONFIG_FILENAME = "roughlyenoughitems/config.json5";
    private static final String REI_CONFIG_SECTION = "appearance";
    private static final String REI_CONFIG_FIELD = "horizontalEntriesBoundariesColumns";
    private static final int HORIZONTALENTRIESBOUNDARIESCOLUMS_VALUE = 16;

    private static final Gson GSON_WRITER = new GsonBuilder().setPrettyPrinting().create();

    public void initializePatches(){
        if (FabricLoader.getInstance().isModLoaded(REI_MOD_ID)) {
            Util.notifyAll("YourMod: REI detected. Attempting to modify REI config.");
            modifyReiConfigWithGson();
        }
    }

    private void modifyReiConfigWithGson() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path reiConfigFile = configDir.resolve(REI_CONFIG_FILENAME);

        if (!Files.exists(reiConfigFile)) {
            Util.notifyAll("YourMod: Could not find REI config file at: " + reiConfigFile, notificationTypes.ERROR);
            return;
        }

        JsonObject rootObject = null;

        try (BufferedReader reader = Files.newBufferedReader(reiConfigFile, StandardCharsets.UTF_8)) {
            JsonElement rootElement = JsonParser.parseReader(reader);

            if (rootElement.isJsonObject()) {
                rootObject = rootElement.getAsJsonObject();
            } else {
                Util.notifyAll("YourMod: REI config root is not a JSON object: " + reiConfigFile, notificationTypes.ERROR);
                return;
            }

        } catch (JsonSyntaxException e) {
            Util.notifyAll("YourMod: Failed to parse REI config file (likely due to non-standard JSON5 features like comments that Gson couldn't handle, or actual syntax errors): " + reiConfigFile, notificationTypes.ERROR);
            return;
        } catch (IOException e) {
            Util.notifyAll("YourMod: Failed to read REI config file: " + reiConfigFile, notificationTypes.ERROR);
            return;
        }

        try {
            if (rootObject != null && rootObject.has(REI_CONFIG_SECTION) && rootObject.get(REI_CONFIG_SECTION).isJsonObject()) {
                JsonObject appearanceObject = rootObject.getAsJsonObject(REI_CONFIG_SECTION);

                if (appearanceObject.has(REI_CONFIG_FIELD)) {
                    JsonElement currentValue = appearanceObject.get(REI_CONFIG_FIELD);
                    Util.notifyAll("YourMod: Current REI value for '" + REI_CONFIG_SECTION + "." + REI_CONFIG_FIELD + "': " + currentValue, notificationTypes.ERROR);
                } else {
                    Util.notifyAll("YourMod: Key '" + REI_CONFIG_SECTION + "." + REI_CONFIG_FIELD + "' not found in REI config.", notificationTypes.ERROR);
                }

                appearanceObject.addProperty(REI_CONFIG_FIELD, HORIZONTALENTRIESBOUNDARIESCOLUMS_VALUE);
                Util.notifyAll("YourMod: Set REI value for '" + REI_CONFIG_SECTION + "." + REI_CONFIG_FIELD + "' to: " + HORIZONTALENTRIESBOUNDARIESCOLUMS_VALUE, notificationTypes.ERROR);

            } else {
                Util.notifyAll("YourMod: REI config structure unexpected. Missing '" + REI_CONFIG_SECTION + "' object.", notificationTypes.ERROR);
                return;
            }
        } catch (Exception e) {
            Util.notifyAll("YourMod: Error modifying the JSON structure in memory.", notificationTypes.ERROR);
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(reiConfigFile, StandardCharsets.UTF_8)) {
            GSON_WRITER.toJson(rootObject, writer);
            Util.notifyAll("YourMod: Successfully saved modified REI config (comments removed): " + reiConfigFile, notificationTypes.ERROR);
        } catch (IOException e) {
            Util.notifyAll("YourMod: Failed to write modified REI config file: " + reiConfigFile, notificationTypes.ERROR);
        }
    }
}
