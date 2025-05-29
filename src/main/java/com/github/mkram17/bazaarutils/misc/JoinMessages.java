package com.github.mkram17.bazaarutils.misc;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.events.BUListener;
import com.github.mkram17.bazaarutils.utils.Util;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class JoinMessages implements BUListener {
    private static final Text welcomeMessage = Text.literal("[Bazaar utils] ")
            .formatted(Formatting.WHITE)
            .append(Text.literal("Thanks for installing! Use /buconfig to configure the mod.")
                    .formatted(Formatting.GREEN));
    private static final Text discordMessage = Text.literal("[Bazaar utils] ")
            .formatted(Formatting.WHITE)
            .append(Text.literal("For more help or to report a bug, join the ")
                    .formatted(Formatting.GREEN)
                    .append(Util.DISCORDLINK)
                    .append(Text.literal("!")
                            .formatted(Formatting.GREEN)));

    @Override
    public void subscribe(){
        registerWelcomeMessageSender();
    }
    private static void registerWelcomeMessageSender() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.player != null) {
                if (BUConfig.get().firstLoad) {
                    Util.tickExecuteLater(40, () -> {
                        client.player.sendMessage(welcomeMessage, false);
                        Util.tickExecuteLater(60, () -> {
                            Util.notifyAll(Util.HELPMESSAGE);

                            Util.tickExecuteLater(40, () -> {
                                client.player.sendMessage(discordMessage, false);
                            });

                        });
                    });
                    BUConfig.get().firstLoad = false;
                    BUConfig.HANDLER.save();
                }
            }
        });
    }

}
