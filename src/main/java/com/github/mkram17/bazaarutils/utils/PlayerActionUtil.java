package com.github.mkram17.bazaarutils.utils;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PlayerActionUtil {
    public static void runCommand(String command){
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.networkHandler.sendChatCommand(command);
        }
    }

    static void sendPlayerMessage(Text message){
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(message, false);
        } else {
            Util.logMessage("Could not send notification because player is null. Message: " + message);
            Util.tickExecuteLater(100, () -> sendPlayerMessage(message));
        }
    }

    public static void notifyAll(Text message) {
        MutableText messageText = Text.literal("[" + BazaarUtils.MOD_NAME + "] ").formatted(Formatting.GOLD);
        messageText.append(message.copy());

        sendPlayerMessage(messageText);
        Util.logMessage(message.getString());
    }

    public static void notifyAll(String message) {
        notifyAll(Text.literal(message).formatted(Formatting.WHITE));
    }

    //only used for developer messages and debugging. notifyAll(String/Text messsage) is used to send messages to the player
    public static void notifyAll(String message, Util.notificationTypes notificationType) {
        String callingName = Util.getCallingClassName();
        String simpleCallingName = callingName.substring(callingName.lastIndexOf(".") + 1);
        MutableText messageText = Text.literal("(" + simpleCallingName + ") ")
                .formatted(Formatting.GOLD)
                .append(Text.literal(message).formatted(Formatting.DARK_GREEN));

        if(notificationType.isEnabled() || BUConfig.get().developer.allMessages)
            notifyAll(messageText);
    }

    public static void notifyChatCommand(MutableText message, String command){
        message.styled(style -> style
                                    .withClickEvent(new ClickEvent.RunCommand("/" + command))
                                    .withHoverEvent(new HoverEvent.ShowText(Text.literal("Run /" + command))));

        notifyAll(message);
    }
}
