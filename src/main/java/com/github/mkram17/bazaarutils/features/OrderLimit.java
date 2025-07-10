package com.github.mkram17.bazaarutils.features;

import java.util.Collections;
import java.util.List;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.misc.ItemSlotButtonWidget;
import com.github.mkram17.bazaarutils.mixin.AccessorHandledScreen;
import com.github.mkram17.bazaarutils.utils.GUIUtils;

import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class OrderLimit {
    private static final double LIMIT_COINS = 10000000000d;
    private static final Identifier BASE = Identifier.tryParse(BazaarUtils.MODID, "widget/widget_settings_base");
    private static final Identifier HOVER = Identifier.tryParse(BazaarUtils.MODID, "widget/widget_settings_hover");
    public static final ButtonTextures SLOT_BUTTON_TEXTURES = new ButtonTextures(
            BASE,
            HOVER);

    @Getter
    private static long orderedCoins;

    private static String orderedCoinsFormatted;

    public static void init() {
        orderedCoins = BUConfig.get().getDailyLimit();
        orderedCoinsFormatted = formatNumberWithPrefix(orderedCoins);
    }

    public static void resetLimit() {
        orderedCoins = 0;
        BUConfig.get().setDailyLimit(0);
        orderedCoinsFormatted = "0";
    }

    public static String formatNumberWithPrefix(double number) {
        String prefix;
        double value;

        if (number >= 1_000_000_000) {
            prefix = "B";
            value = number / 1_000_000_000.0;
        } else {
            prefix = "M";
            value = number / 1_000_000.0;
        }

        return String.format("%.2f", value) + prefix;
    }

    public static void addLimit(double price) {
        orderedCoins += price;
        BUConfig.get().setDailyLimit(orderedCoins);
        orderedCoinsFormatted = formatNumberWithPrefix(orderedCoins);
        BUConfig.HANDLER.save();
    }

    public static List<ItemSlotButtonWidget> getWidget() {
        boolean isTargetScreen = GUIUtils.inBazaar();
        if (!(MinecraftClient.getInstance().currentScreen instanceof AccessorHandledScreen screen) || !isTargetScreen)
            return Collections.emptyList();

        String screenTitle = MinecraftClient.getInstance().currentScreen.getTitle().getString();

        ItemSlotButtonWidget.ScreenWidgetDimensions dimensions = ItemSlotButtonWidget.getSafeScreenDimensions(screen,
                screenTitle);

        int buttonSize = 18;
        int spacing = 4;
        int buttonX = dimensions.x() - buttonSize - spacing;
        int currentButtonY = dimensions.y() + spacing - 22;

        ItemSlotButtonWidget button = new ItemSlotButtonWidget(
                buttonX,
                currentButtonY,
                buttonSize, buttonSize,
                SLOT_BUTTON_TEXTURES,
                (btn) -> {
                },
                null,
                Text.literal("Bazaar Order Limit: " + orderedCoinsFormatted + "/ 10B"));
        return Collections.singletonList(button);
    }
}
