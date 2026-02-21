package com.github.mkram17.bazaarutils.features.chat;

import com.github.mkram17.bazaarutils.config.util.ConfigUtil;
import com.github.mkram17.bazaarutils.events.listener.BUListener;
import com.github.mkram17.bazaarutils.features.util.BUToggleableFeature;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.Util;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption;
import com.teamresourceful.resourcefulconfig.api.types.info.TooltipProvider;
import lombok.Getter;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;

import java.util.Arrays;

@ConfigObject
public class UselessBazaarNotificationsRemover extends BUListener implements BUToggleableFeature {
    public enum TransientBazaarMessages implements TooltipProvider {
        CANCELLING_ORDER("[Bazaar] Cancelling order..."),
        PUTTING_GOODS_IN_ESCROW("[Bazaar] Putting goods in escrow..."),
        SUBMITTING_BUY_ORDER("[Bazaar] Submitting buy order..."),
        CLAIMING_ORDER("[Bazaar] Claiming order..."),
        SUBMITTING_SELL_OFFER("[Bazaar] Submitting sell offer..."),
        EXECUTING_INSTANT_SELL("[Bazaar] Executing instant sell..."),
        EXECUTING_INSTANT_BUY("[Bazaar] Executing instant buy..."),
        CLAIMING_ORDERS("[Bazaar] Claiming orders...");

        @Getter
        private final String message;

        TransientBazaarMessages(String message) {
            this.message = message;
        }

        @Override
        public Text getTooltip() {
            return Text.of(getMessage());
        }
    }

    @Getter
    @ConfigEntry(
            id = "enabled",
            translation = "bazaarutils.config.chat.useless_bazaar_notifications_remover.enabled.value"
    )
    public boolean enabled;

    @ConfigEntry(id = "first_time_removed")
    @ConfigOption.Hidden
    public boolean firstTimeRemoved = true;

    @ConfigEntry(
            id = "excluded_notifications",
            translation = "bazaarutils.config.chat.useless_bazaar_notifications_remover.excluded_notifications.value"
    )
    @Comment(
            value = "The list of transient messages/notifications to be excluded/unallowed from the chat",
            translation = "bazaarutils.config.chat.useless_bazaar_notifications_remover.excluded_notifications.description"
    )
    @ConfigOption.Draggable()
    public static TransientBazaarMessages[] excludedNotifications = new TransientBazaarMessages[]{
            TransientBazaarMessages.CANCELLING_ORDER,
            TransientBazaarMessages.PUTTING_GOODS_IN_ESCROW,
            TransientBazaarMessages.SUBMITTING_BUY_ORDER,
            TransientBazaarMessages.CLAIMING_ORDER,
            TransientBazaarMessages.SUBMITTING_SELL_OFFER,
            TransientBazaarMessages.EXECUTING_INSTANT_SELL,
            TransientBazaarMessages.EXECUTING_INSTANT_BUY,
            TransientBazaarMessages.CLAIMING_ORDERS,
    };

    public UselessBazaarNotificationsRemover(boolean enabled) {
        this.enabled = enabled;
    }

    private void registerUselessNotificationDetector(){
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (!enabled) {
                return true;
            }

            if (isNotificationUseless(message.getString())) {
                if (firstTimeRemoved) {
                    firstTimeRemoved = false;
                    Util.tickExecuteLater(2, () -> PlayerActionUtil.notifyAll("TIP - Useless Bazaar notifications such as \"Putting goods in escrow...\" are removed by default! " +
                            "To disable this feature, uncheck the \"Remove Useless Bazaar Notifications\" option in the Bazaar Utils settings."));
                    ConfigUtil.scheduleConfigSave();
                }

                return false;
            }

            return true;
        });
    }

    private static boolean isNotificationUseless(String message) {
        return Arrays.stream(excludedNotifications).anyMatch(n -> message.contains(n.getMessage()));
    }

    @Override
    protected void registerFabricEvents() {
        super.subscribeToMeteorEventBus = false;
        registerUselessNotificationDetector();
    }
}
