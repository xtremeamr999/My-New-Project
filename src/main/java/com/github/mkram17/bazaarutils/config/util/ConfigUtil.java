package com.github.mkram17.bazaarutils.config.util;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.utils.Util;
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;

import java.util.ArrayList;
import java.util.List;

import static com.github.mkram17.bazaarutils.config.BUConfig.CONFIGURATOR;

public class ConfigUtil {

    private static boolean configSaveScheduled = false;

    public static void registerConfig(){
        CONFIGURATOR.register(BUConfig.class);
    }
    public static Screen createGUI(Screen parent) {
        return ResourcefulConfigScreen.make(CONFIGURATOR, BUConfig.class)
                .withParent(parent)
                .build();
    }

    public static void openGUI() {
        MinecraftClient client = MinecraftClient.getInstance();
        Screen parent = client.currentScreen;
        client.send(() -> client.setScreen(createGUI(parent)));
    }

    public static void scheduleConfigSave() {
        if (!configSaveScheduled) {
            configSaveScheduled = true;
            Util.tickExecuteLater(20, () -> { // 1 second
                CONFIGURATOR.saveConfig(BUConfig.class);
                configSaveScheduled = false;
            });
        }
    }

    public static List<ClickableWidget> getWidgets(){
        List<ClickableWidget> widgets = new ArrayList<>();
        //automatically added using @RegisterWidget annotation
        return widgets;
    }
}
