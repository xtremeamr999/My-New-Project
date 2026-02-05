package com.github.mkram17.bazaarutils.config.developer;

import com.github.mkram17.bazaarutils.misc.NotificationType;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;

public class DeveloperConfig {

    @ConfigEntry(id = "developerMode")
    public boolean developerMode = false;

    @ConfigEntry(id = "disableErrorNotifications")
    public boolean disableErrorNotifications = false;

    @ConfigEntry(id = "allMessages")
    public boolean allMessages = false;
    @ConfigEntry(id = "errorMessages")
    public boolean errorMessages = false;
    @ConfigEntry(id = "guiMessages")
    public boolean guiMessages = false;
    @ConfigEntry(id = "featureMessages")
    public boolean featureMessages = false;
    @ConfigEntry(id = "bazaarDataMessages")
    public boolean bazaarDataMessages = false;
    @ConfigEntry(id = "commandMessages")
    public boolean commandMessages = false;
    @ConfigEntry(id = "itemDataMessages")
    public boolean itemDataMessages = false;

    public boolean isDeveloperVariableEnabled(NotificationType type) {
        return switch (type) {
            case GUI -> guiMessages;
            case FEATURE -> featureMessages;
            case BAZAARDATA -> bazaarDataMessages;
            case COMMAND -> commandMessages;
            case ORDERDATA -> itemDataMessages;
            case ERROR -> errorMessages;
        };
    }

}
