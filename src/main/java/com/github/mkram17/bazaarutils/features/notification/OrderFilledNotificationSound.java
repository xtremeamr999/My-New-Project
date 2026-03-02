package com.github.mkram17.bazaarutils.features.notification;

import com.github.mkram17.bazaarutils.config.features.notification.NotificationsConfig;
import com.github.mkram17.bazaarutils.utils.annotations.modules.Module;
import com.github.mkram17.bazaarutils.utils.config.BUToggleableFeature;

@Module
public class OrderFilledNotificationSound implements BUToggleableFeature {
    @Override
    public boolean isEnabled() {
        return NotificationsConfig.ORDER_NOTIFICATIONS_FILLED.isEnabled();
    }

    public OrderFilledNotificationSound() {}
}
