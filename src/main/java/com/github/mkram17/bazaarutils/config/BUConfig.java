package com.github.mkram17.bazaarutils.config;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.data.GeneralDataConfig;
import com.github.mkram17.bazaarutils.config.data.MetadataConfig;
import com.github.mkram17.bazaarutils.config.features.DeveloperConfig;
import com.github.mkram17.bazaarutils.config.features.FeatureConfig;
import com.github.mkram17.bazaarutils.config.features.KeybindConfig;
import com.github.mkram17.bazaarutils.config.features.chat.ChatConfig;
import com.github.mkram17.bazaarutils.config.features.gui.ButtonsConfig;
import com.github.mkram17.bazaarutils.config.features.gui.InventoryConfig;
import com.github.mkram17.bazaarutils.config.features.gui.OverlaysConfig;
import com.github.mkram17.bazaarutils.config.features.notification.NotificationsConfig;
import com.teamresourceful.resourcefulconfig.api.annotations.*;
import com.teamresourceful.resourcefulconfig.api.loader.Configurator;
import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfig;

@Config(
        value = BazaarUtils.MOD_ID,
        categories = {
                GeneralDataConfig.class,
                MetadataConfig.class,
                ChatConfig.class,
                ButtonsConfig.class,
                InventoryConfig.class,
                OverlaysConfig.class,
                NotificationsConfig.class,
                DeveloperConfig.class
        }
)
@ConfigInfo(
        title = "Bazaar Utils",
        description = "A QOL mod for Hypixel Skyblock focused on the bazaar.",
        links = {
                @ConfigInfo.Link(
                        value = "https://modrinth.com/mod/bazaar-utils",
                        icon = "modrinth",
                        text = "Modrinth"
                )
        }
)
public final class BUConfig {

    private static final BUConfig INSTANCE = new BUConfig();

    public static BUConfig get(){
        return INSTANCE;
    }

    @ConfigEntry(id = "introductorySeparator")
    @ConfigOption.Hidden
    @ConfigOption.Separator(
            value = "bazaarutils.config.separator.introductory.value",
            description = "bazaarutils.config.separator.introductory.description"
    )
    public static boolean INTRODUCTORY_SEPARATOR = true;

    public GeneralDataConfig general = new GeneralDataConfig();
    public MetadataConfig metadata = new MetadataConfig();
    public DeveloperConfig developer = new DeveloperConfig();
    public FeatureConfig feature = new FeatureConfig();
    public KeybindConfig keybind = new KeybindConfig();

    public static ResourcefulConfig register(Configurator configurator) {
        configurator.register(BUConfig.class);

        return configurator.getConfig(BUConfig.class);
    }
}
