package com.github.mkram17.bazaarutils.misc;

import com.github.mkram17.bazaarutils.config.features.DeveloperConfig;

public enum NotificationType {

    GUI, FEATURE, BAZAARDATA, COMMAND, ORDERDATA, ERROR;

    public boolean isEnabled() {
        return DeveloperConfig.isDeveloperVariableEnabled(this);
    }
}
