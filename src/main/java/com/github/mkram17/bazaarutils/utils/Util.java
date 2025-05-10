package com.github.mkram17.bazaarutils.utils;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.data.BazaarData;
import com.github.mkram17.bazaarutils.misc.ItemData;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@UtilityClass
public class Util {
    public static void sendCommand(String command){
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.networkHandler.sendChatCommand(command);
        }
    }

    public enum notificationTypes {
        ERROR, GUI, FEATURE, BAZAARDATA, COMMAND, ITEMDATA;
        public boolean isEnabled() {
            return BUConfig.get().developer.isDeveloperVariableEnabled(this);
        }
    }
    private static final LinkedList<ScheduledTask> tasks = new LinkedList<>();
    public static final String HELPMESSAGE = "Commands: /bu or /bazaarutils to open settings gui. \n---------------------------\n " +
            "/bu customorders to see current Custom Orders. /bu customorder {order amount} {slot number} to make new Custom Order /bu customorder remove {customorder number} to remove Custom Order (find number by using /bu customorders) \n---------------------------\n  ";
    public static final Text DISCORDLINK = Text.literal("Discord server")
            .styled(style -> {
                        //? if > 1.21.4 {
                        try {
                            return style
                                            .withClickEvent(new ClickEvent.OpenUrl(new URI("https://discord.gg/xDKjvm5hQd")))
                            .withHoverEvent(new HoverEvent.ShowText(Text.literal("Click to join the Discord!")));
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    //?} else {
                        /*return style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/xDKjvm5hQd"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to join the Discord!")));
                    });
        *///?}

    public static<T> void notifyAll(T message) {
        String callingName = getCallingClassName();
        String messageStr = message.toString();
        messageStr = messageStr.toLowerCase().contains("exception") ? "§c" + messageStr : "§a" + messageStr;

        if (MinecraftClient.getInstance().player != null)
            MinecraftClient.getInstance().player.sendMessage(Text.literal("[BazaarUtils] " + messageStr), false);
        LogManager.getLogger(callingName).info("[Bazaar utils] Message [" + message + "]");
    }

    public static<T> void notifyAll(T message, notificationTypes notiType) {
        String callingName = getCallingClassName();
        String simpleCallingName = callingName.substring(callingName.lastIndexOf(".") + 1);
        String messageStr = notiType == notificationTypes.ERROR ? "§c" + message : "§a" + message;
        Text messageText = Text.literal("[" + simpleCallingName + "] " + messageStr);
        if(notiType == notificationTypes.ERROR){
            messageText = Text.literal("[Bazaar utils] ERROR: " + messageText.getString() + ". Click here for support.").styled(style -> {
                        //? if > 1.21.4 {
                try {
                    return style
                            .withColor(Formatting.RED)
                            .withClickEvent(new ClickEvent.OpenUrl(new URI("https://discord.gg/xDKjvm5hQd")))
                            .withHoverEvent(new HoverEvent.ShowText(Text.literal("Click to join the Discord for support")));
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
            //?} else {
                        /*return style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/xDKjvm5hQd"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to join the Discord for support")));
                    });
            *///?}
        }

        if (notiType.isEnabled() || BUConfig.get().developer.allMessages || notiType == notificationTypes.ERROR) {
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(messageText, false);
            }
            LogManager.getLogger(callingName).info("[Bazaar utils] Message [" + message + "]");
        }
    }

    public static<T> void executeLater(Runnable runnable, int milliDelay){
        try {
            Thread.sleep(milliDelay);
            MinecraftClient.getInstance().execute(runnable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void notifyChatCommand(String message, String command){
        assert MinecraftClient.getInstance().player != null;
        MinecraftClient.getInstance().player.sendMessage(Text.literal(message)
                .styled(style -> style
                        .withBold(true)
                        .withColor(Formatting.GOLD)
                        //? if > 1.21.4 {
                        .withClickEvent(new ClickEvent.RunCommand("/" + command))
                        .withHoverEvent(new HoverEvent.ShowText(Text.literal("Run /" + command)))
                        //?} else {
                                /*.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Run /" + command)))
                        *///?}
                ), false);
    }

    public static void addWatchedItem(String itemName, Double fullPrice, boolean isSellOrder, int volume) {
        itemName = itemName.toLowerCase();
        if (BazaarData.findProductId(itemName) != null) {
            ItemData.priceTypes type = isSellOrder ? ItemData.priceTypes.INSTABUY : ItemData.priceTypes.INSTASELL;
            ItemData itemToAdd = new ItemData(itemName, fullPrice, type, volume);
            BUConfig.get().watchedItems.add(itemToAdd);
            notifyAll("Added item: § " + itemToAdd.getGeneralInfo(), notificationTypes.ITEMDATA);
        } else {
            notifyAll("Could not add item: § " + itemName + "  (is it spelled correctly?)", notificationTypes.ERROR);
        }
        BUConfig.HANDLER.save();
        ItemData.update();
    }

    public static void addWatchedItem(ItemData item){
        if(item == null)
            return;
        assert item.getProductID() != null;
        BUConfig.get().watchedItems.add(item);
        notifyAll("Added item: § " + item.getGeneralInfo(), notificationTypes.ITEMDATA);
        BUConfig.HANDLER.save();
        ItemData.update();
    }

    private static void subscribeTicks(){
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            List<ScheduledTask> snapshot = new ArrayList<>(tasks);
            for (ScheduledTask task : snapshot) {
                task.ticksLeft--;
                if (task.ticksLeft <= 0) {
                    task.action.run();
                    tasks.remove(task);
                }
            }
        });
    }


    //this one runs asynch and other one runs on main thread (i think)
    public static void tickExecuteLater(int ticks, Runnable action) {
        tasks.add(new ScheduledTask(ticks, action));
    }

    private static class ScheduledTask {
        int ticksLeft;
        Runnable action;

        ScheduledTask(int ticks, Runnable action) {
            this.ticksLeft = ticks;
            this.action = action;
        }
    }

    public static void startExecutors() {
        BazaarData.scheduleBazaar();
        subscribeTicks();
    }

    public static String getCallingClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return stackTrace[3].getClassName().substring(stackTrace[3].getClassName().lastIndexOf(".") + 1);
    }

    //finds the first index that contains lookingFor, so there could be another later which would cause problems
    public static int findComponentIndex(List<Text> components, String lookingFor){
        int num = 0;
        for(Text component : components){
            if(component.getString().contains(lookingFor))
                return num;
            num++;
        }
            return -1;
    }
    public static String findComponentWith(List<Text> components, String lookingFor){
        for(Text component : components){
            if(component.getString().contains(lookingFor))
                return component.getString();
        }
            return null;
    }

    public static void copyToClipboard(String clip) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(clip), null);
    }

    public static String removeFormatting(String str) {
        return str.replaceAll("§.", "").replace(",", "").trim();
    }
    public static int parseNumber(String input) {
        input = input.toUpperCase();
        double value = Double.parseDouble(input.replaceAll("[^0-9.]", ""));

        if (input.endsWith("K")) return (int) (value * 1_000);
        if (input.endsWith("M")) return (int) (value * 1_000_000);
        if (input.endsWith("B")) return (int) (value * 1_000_000_000);

        return (int) value;
    }
    public static <T> void writeFile(T content) {
        try {
            Files.write(Paths.get("bazaar_data.json"), content.toString().getBytes());
            notifyAll("Data written to file successfully.");
        } catch (Exception e) {
            System.out.println("Failed to write data to file");
            e.printStackTrace();
        }
    }

    public static String getClipboardContents() {
        try {
            return (String) Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .getData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            return null;
        }
    }

    public static String capAtMinecraftLength(String input, int limit) {
        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        return capAtLength(input, limit, c -> renderer.getWidth(String.valueOf(c)));
    }

    private static String capAtLength(String input, int limit, LengthJudger lengthJudger) {
        int currentLength = 0;
        int index = 0;
        for (char c : input.toCharArray()) {
            currentLength += lengthJudger.judgeLength(c);
            if (currentLength >= limit) break;
            index++;
        }
        return input.substring(0, index);
    }
    public static String extractTextAfterWord(String text, String word) {
        if (text == null || word == null || text.isEmpty() || word.isEmpty()) {
            return "";
        }

        int wordIndex = text.indexOf(word);
        if (wordIndex == -1) {
            return ""; // Word not found
        }

        // Start looking after the word
        int startIndex = wordIndex + word.length();
        if (startIndex >= text.length()) {
            return ""; // Word is at the end of the text
        }

        // Skip spaces after the word
        while (startIndex < text.length() && Character.isWhitespace(text.charAt(startIndex))) {
            startIndex++;
        }

        if (startIndex >= text.length()) {
            return ""; // No non-space characters after the word
        }

        // Find the next space after non-space content
        int endIndex = startIndex;
        while (endIndex < text.length() && !Character.isWhitespace(text.charAt(endIndex))) {
            endIndex++;
        }

        return removeFormatting(text.substring(startIndex, endIndex));
    }
    public static double removeTrailingZeroes(double value) {
        return Double.parseDouble(String.valueOf(value).replaceAll("\\.0$", "").replaceAll("(\\.\\d*?)0+$", "$1"));
    }

    public static double truncateNumber(double number) {
        return Math.round(number * 100) / 100.0;
    }

    public static double getPrettyNumber(double num) {
        return truncateNumber(removeTrailingZeroes(num));
    }

    @FunctionalInterface
    public interface LengthJudger {
        int judgeLength(char c);
    }
}