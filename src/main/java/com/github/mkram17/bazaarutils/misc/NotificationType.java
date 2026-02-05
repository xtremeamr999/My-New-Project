package com.github.mkram17.bazaarutils.misc;

import com.github.mkram17.bazaarutils.config.ResourcefulConfig;

public enum NotificationType {

    GUI, FEATURE, BAZAARDATA, COMMAND, ORDERDATA, ERROR;

    public boolean isEnabled() {
        return ResourcefulConfig.INSTANCE.developer.isDeveloperVariableEnabled(this);
    }
}
