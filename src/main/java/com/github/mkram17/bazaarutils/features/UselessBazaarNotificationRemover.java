package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.events.handlers.BUListener;
import com.github.mkram17.bazaarutils.features.util.ToggleableFeature;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.Util;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import java.util.Arrays;

public class UselessBazaarNotificationRemover implements BUListener, ToggleableFeature {
    @Getter @Setter
    private boolean enabled;
    private boolean firstTimeRemoved = true;

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
            if(isNotificationUseless(message.getString())) {
                if(firstTimeRemoved) {
                    Util.tickExecuteLater(2, () -> PlayerActionUtil.notifyAll("TIP - Useless Bazaar notifications such as \"Putting goods in escrow...\" are removed by default! " +
                            "To disable this feature, uncheck the \"Remove Useless Bazaar Notifications\" option in the Bazaar Utils settings."));
                    firstTimeRemoved = false;
                    BUConfig.scheduleConfigSave();
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
    public void subscribe() {
        registerUselessNotificationDetector();
    }

    @Override
    public void createOption(ConfigCategory.Builder builder) {
        builder.option(ToggleableFeature.createOptionHelper("Remove Useless Bazaar Notifications",
                "Removes useless notifications that appear when making, claiming, and cancelling orders. Eg 'Putting goods in escrow...'",
                true,
                this::isEnabled, this::setEnabled));
    }
}
