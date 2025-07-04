package com.github.mkram17.bazaarutils.utils;

import com.github.mkram17.bazaarutils.config.BUConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PlayerActionUtil {
    public static void useCommand(String command){
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.networkHandler.sendChatCommand(command);
        }
    }

    static void sendPlayerMessage(Text message){
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(message, false);
        } else {
            Util.logError("Could not send notification because player is null. Message: " + message, null);
            Util.tickExecuteLater(50, () -> sendPlayerMessage(message));
        }
    }

    public static void notifyAll(Text message) {
        MutableText messageText = Text.literal("[Bazaar Utils] ").formatted(Formatting.GOLD);
        messageText.append(message.copy().formatted(Formatting.WHITE));

        sendPlayerMessage(messageText);
        Util.logMessage(message.getString());
    }

    public static void notifyAll(String message) {
        notifyAll(Text.literal(message));
    }

    private static void notifyAll(Text message, Util.notificationTypes notiType) {
        String callingName = Util.getCallingClassName();
        String simpleCallingName = callingName.substring(callingName.lastIndexOf(".") + 1);
        Text messageText = Text.literal("(" + simpleCallingName + ") ")
                .formatted(Formatting.GOLD)
                    .append(message)
                    .formatted(Formatting.DARK_GREEN);

        if(notiType.isEnabled() || BUConfig.get().developer.allMessages)
            notifyAll(messageText);
    }

    //only used for developer messages and debugging. notifyAll(String messsage) is used to send messages to the player
    public static void notifyAll(String message, Util.notificationTypes notiType) {
        notifyAll(Text.literal(message), notiType);
    }

    public static void notifyChatCommand(MutableText message, String command){
        message.styled(style -> style
                                    //? if > 1.21.4 {
                                    .withClickEvent(new ClickEvent.RunCommand("/" + command))
                                    .withHoverEvent(new HoverEvent.ShowText(Text.literal("Run /" + command))));
                            //?} else {
                                /*.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Run /" + command))));
                        *///?}
        notifyAll(message);
    }
}
