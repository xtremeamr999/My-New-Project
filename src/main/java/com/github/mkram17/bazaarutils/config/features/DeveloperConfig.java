package com.github.mkram17.bazaarutils.config.features;

import com.github.mkram17.bazaarutils.misc.NotificationType;
import com.teamresourceful.resourcefulconfig.api.annotations.*;

@Category(
        value = "developer_config",
        categories = {
                DeveloperConfig.DebugMessages.class
        }
)
@ConfigInfo(
        title = "Developer Config",
        titleTranslation = "bazaarutils.config.developer.category.value",
        description = "Developer configurations & toggleable tools of the mod",
        descriptionTranslation = "bazaarutils.config.developer.category.description",
        icon = "code-2"
)
public final class DeveloperConfig {
    @ConfigEntry(
            id = "enabled",
            translation = "bazaarutils.config.developer.enabled.value"
    )
    @Comment(
            value = "Global toggle for all developer related functionalities/utilities.",
            translation = "bazaarutils.config.developer.enabled.description"
    )
    public static boolean enabled = false;

    @ConfigEntry(
            id = "disableErrorNotifications",
            translation = "bazaarutils.config.developer.disableErrorNotifications.value"
    )
    public static boolean disableErrorNotifications = false;

    @Category("debug_messages")
    @ConfigInfo(
            title = "Debug Messages",
            titleTranslation = "bazaarutils.config.developer.debugMessages.category.value",
            description = "Debug Messages",
            descriptionTranslation = "bazaarutils.config.developer.debugMessages.category.description",
            icon = "megaphone"
    )
    public static final class DebugMessages {
        @ConfigEntry(
                id = "all",
                translation = "bazaarutils.config.developer.debugMessages.all.value"
        )
        public static boolean all = false;

        @ConfigEntry(
                id = "errors",
                translation = "bazaarutils.config.developer.debugMessages.errors.value"
        )
        public static boolean errors = false;

        @ConfigEntry(
                id = "features",
                translation = "bazaarutils.config.developer.debugMessages.features.value"
        )
        public static boolean features = false;

        @ConfigEntry(
                id = "bazaarData",
                translation = "bazaarutils.config.developer.debugMessages.bazaarData.value"
        )
        public static boolean bazaarData = false;

        @ConfigEntry(
                id = "itemData",
                translation = "bazaarutils.config.developer.debugMessages.itemData.value"
        )
        public static boolean itemData = false;

        @ConfigEntry(
                id = "gui",
                translation = "bazaarutils.config.developer.debugMessages.gui.value"
        )
        public static boolean gui = false;

        @ConfigEntry(
                id = "commands",
                translation = "bazaarutils.config.developer.debugMessages.commands.value"
        )
        public static boolean commands = false;

        public static boolean isDeveloperVariableEnabled(NotificationType type) {
            return switch (type) {
                case GUI -> gui;
                case FEATURE -> features;
                case BAZAARDATA -> bazaarData;
                case COMMAND -> commands;
                case ORDERDATA -> itemData;
                case ERROR -> errors;
            };
        }
    }


}
