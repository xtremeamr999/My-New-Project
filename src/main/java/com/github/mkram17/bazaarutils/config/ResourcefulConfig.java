package com.github.mkram17.bazaarutils.config;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.data.GeneralDataConfig;
import com.github.mkram17.bazaarutils.config.data.MetadataConfig;
import com.github.mkram17.bazaarutils.config.developer.DeveloperConfig;
import com.github.mkram17.bazaarutils.config.feature.FeatureConfig;
import com.github.mkram17.bazaarutils.features.keybinds.StashHelper;
import com.github.mkram17.bazaarutils.utils.Util;
import com.teamresourceful.resourcefulconfig.api.annotations.Config;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen;
import com.teamresourceful.resourcefulconfig.api.loader.Configurator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@ConfigInfo(title = "Bazaar Utils",
        description = "A QOL mod for Hypixel Skyblock focused on the bazaar.",
        links = {
                @ConfigInfo.Link(
                        value = "https://modrinth.com/mod/bazaar-utils",
                        icon = "modrinth",
                        text = "Modrinth"
                )})
@Config(value = BazaarUtils.MODID)
public final class ResourcefulConfig {

    public static final ResourcefulConfig INSTANCE = new ResourcefulConfig();
    private static final Configurator CONFIGURATOR = new Configurator(BazaarUtils.MODID);

    public GeneralDataConfig general = new GeneralDataConfig();
    public MetadataConfig metadata = new MetadataConfig();
    public DeveloperConfig developer = new DeveloperConfig();
    public FeatureConfig feature = new FeatureConfig();

    private static boolean configSaveScheduled = false;

    //Keybinds (must be static). They get registered on object creation.
    public static StashHelper stashHelper = new StashHelper(new KeyBinding(
            "Pick Up Stash",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            StashHelper.CATEGORY
    ));


    public static void registerConfig(){
        CONFIGURATOR.register(ResourcefulConfig.class);
    }
    public static Screen createGUI(Screen parent) {
        return ResourcefulConfigScreen.make(CONFIGURATOR, ResourcefulConfig.class)
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
                CONFIGURATOR.saveConfig(ResourcefulConfig.class);
                configSaveScheduled = false;
            });
        }
    }
}
