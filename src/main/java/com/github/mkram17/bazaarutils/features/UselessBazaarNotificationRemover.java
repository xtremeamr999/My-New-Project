package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.config.util.ConfigUtil;
import com.github.mkram17.bazaarutils.events.listener.BUListener;
import com.github.mkram17.bazaarutils.features.util.BUToggleableFeature;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.Util;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import java.util.Arrays;

@ConfigObject
public class UselessBazaarNotificationRemover extends BUListener implements BUToggleableFeature {

    @Getter @Setter @ConfigEntry(id = "enabled")
    private boolean enabled;
    @ConfigEntry(id = "firstTimeRemoved")
    private boolean firstTimeRemoved = true;

    public UselessBazaarNotificationRemover(boolean enabled) {
        this.enabled = enabled;
    }

    private static final String[] uselessNotifications = {
            "[Bazaar] Cancelling order...",
            "[Bazaar] Putting goods in escrow...",
            "[Bazaar] Submitting buy order...",
            "[Bazaar] Claiming order...",
            "[Bazaar] Submitting sell offer...",
            "[Bazaar] Executing instant sell...",
            "[Bazaar] Executing instant buy...",
            "[Bazaar] Claiming orders..."
    };

    private void registerUselessNotificationDetector(){
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if(!enabled)
                return true;

            if(isNotificationUseless(message.getString())) {
                if(firstTimeRemoved) {
                    Util.tickExecuteLater(2, () -> PlayerActionUtil.notifyAll("TIP - Useless Bazaar notifications such as \"Putting goods in escrow...\" are removed by default! " +
                            "To disable this feature, uncheck the \"Remove Useless Bazaar Notifications\" option in the Bazaar Utils settings."));
                    firstTimeRemoved = false;
                    ConfigUtil.scheduleConfigSave();
                }

                return false;
            }
            return true;
        });
    }

    private static boolean isNotificationUseless(String message){
        return Arrays.stream(uselessNotifications).anyMatch(message::contains);
    }

    @Override
    protected void registerFabricEvents() {
        super.subscribeToMeteorEventBus = false;
        registerUselessNotificationDetector();
    }
}
