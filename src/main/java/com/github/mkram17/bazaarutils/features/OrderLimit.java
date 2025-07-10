package com.github.mkram17.bazaarutils.features;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.events.BUListener;
import com.github.mkram17.bazaarutils.events.ChestLoadedEvent;
import com.github.mkram17.bazaarutils.misc.ItemSlotButtonWidget;
import com.github.mkram17.bazaarutils.mixin.AccessorHandledScreen;
import com.github.mkram17.bazaarutils.utils.GUIUtils;

import com.github.mkram17.bazaarutils.utils.TimeUtil;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class OrderLimit implements BUListener {
    @Getter @Setter
    private boolean enabled;
    private static final double LIMIT_COINS = 10_000_000_000d;
    private static final Identifier BASE = Identifier.tryParse(BazaarUtils.MODID, "widget/widget_settings_base");
    private static final Identifier HOVER = Identifier.tryParse(BazaarUtils.MODID, "widget/widget_settings_hover");
    public static final ButtonTextures SLOT_BUTTON_TEXTURES = new ButtonTextures(
            BASE,
            HOVER);

    @Getter
    private final List<OrderLimitEntry> orderLimitEntries;

    private double getTotalOrderedCoins() {
        return orderLimitEntries.stream().mapToDouble(OrderLimitEntry::price).sum();
    }

    public OrderLimit(boolean enabled) {
        this.enabled = enabled;
        this.orderLimitEntries = new ArrayList<>();
    }

    public void init() {
        BazaarUtils.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    public void onBazaarOpen(ChestLoadedEvent event) {
        if(!GUIUtils.inBazaar()) return;

        //if the most recent order is before the last reset time, reset the limit
        if(orderLimitEntries.getLast().time().isBefore(TimeUtil.LAST_BAZAAR_LIMIT_RESET_TIME)) {
            resetLimit();
        }
    }

    public void resetLimit() {
        orderLimitEntries.clear();
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

    public void addLimit(double price) {
        orderLimitEntries.add(new OrderLimitEntry(price, ZonedDateTime.now()));
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

        String orderedCoinsFormatted = formatNumberWithPrefix(BUConfig.get().orderLimit.getTotalOrderedCoins());

        ItemSlotButtonWidget button = new ItemSlotButtonWidget(
                buttonX,
                currentButtonY,
                buttonSize, buttonSize,
                SLOT_BUTTON_TEXTURES,
                (btn) -> {
                },
                null,
                Text.literal("Bazaar Order Limit: " + orderedCoinsFormatted + "/" + formatNumberWithPrefix(LIMIT_COINS)));
        return Collections.singletonList(button);
    }

    @Override
    public void subscribe() {
        init();
    }

    public record OrderLimitEntry(@Getter double price, @Getter ZonedDateTime time) {
            public OrderLimitEntry(double price, ZonedDateTime time) {
                this.price = price;
                this.time = time;
                BUConfig.HANDLER.save();
            }
        }
}
