package com.github.mkram17.bazaarutils.misc;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.config.util.ConfigUtil;
import com.github.mkram17.bazaarutils.misc.autoregistration.RunOnInit;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.Util;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class JoinMessages {

    private static Text welcomeMessage;
    private static Text discordMessage;
    private static Text updateMessage;

    @RunOnInit(priority = RunOnInit.EVENT_PRIORITIES.HIGH)
    public static void initializeFields(){
        welcomeMessage = Text.literal("Thanks for installing! Use /bu or /bazaarutils to configure the mod.")
                .formatted(Formatting.GREEN);
        discordMessage = Text.literal("For more help or to report a bug, join the ")
                .formatted(Formatting.GREEN)
                .append(Util.DISCORD_TEXT)
                .append(Text.literal("!")
                        .formatted(Formatting.GREEN));
        updateMessage = (Text.literal(BazaarUtils.getUpdateNotes())
                .formatted(Formatting.DARK_GREEN));

    }

    @RunOnInit
    public static void registerWelcomeMessageSender() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (BUConfig.get().metadata.isFirstLoad) {
                sendFirstLoadMessages();
            } else if (BazaarUtils.updatedMajorVersion) {
                sendMajorUpdateMessages();
            }
        });
    }

    private static void sendFirstLoadMessages(){
        Util.tickExecuteLater(40, () -> {
            PlayerActionUtil.notifyAll(welcomeMessage);
            Util.tickExecuteLater(60, () -> {
                PlayerActionUtil.notifyAll(Util.HELP_MESSAGE);
                Util.tickExecuteLater(40, () -> PlayerActionUtil.notifyAll(discordMessage));
            });
        });
        BUConfig.get().metadata.isFirstLoad = false;
        ConfigUtil.scheduleConfigSave();
    }

    private static void sendMajorUpdateMessages(){
        Util.tickExecuteLater(40, () -> PlayerActionUtil.notifyAll(updateMessage));
        Util.tickExecuteLater(41, () -> PlayerActionUtil.notifyAll(Util.CHANGELOG));
        BazaarUtils.updatedMajorVersion = false;
    }

}
