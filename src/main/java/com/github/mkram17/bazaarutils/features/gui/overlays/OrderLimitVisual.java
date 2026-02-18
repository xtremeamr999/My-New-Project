package com.github.mkram17.bazaarutils.features.gui.overlays;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.config.util.ConfigUtil;
import com.github.mkram17.bazaarutils.events.listener.BUListener;
import com.github.mkram17.bazaarutils.features.util.BUToggleableFeature;
import com.github.mkram17.bazaarutils.misc.BUCompatibilityHelper;
import com.github.mkram17.bazaarutils.misc.autoregistration.RegisterWidget;
import com.github.mkram17.bazaarutils.misc.autoregistration.RunOnInit;
import com.github.mkram17.bazaarutils.mixin.AccessorHandledScreen;

import com.github.mkram17.bazaarutils.ui.widgets.ItemSlotButtonWidget;
import com.github.mkram17.bazaarutils.ui.widgets.TextDisplayWidget;
import com.github.mkram17.bazaarutils.utils.ScreenInfo;
import com.github.mkram17.bazaarutils.utils.TimeUtil;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@ConfigObject
public class OrderLimitVisual extends BUListener implements BUToggleableFeature {

    @ConfigObject
    public record OrderLimitEntry(@Getter @ConfigEntry(id = "price") double price, @Getter @ConfigEntry(id = "time") ZonedDateTime time) {
        public OrderLimitEntry(double price, ZonedDateTime time) {
            this.price = price;
            this.time = time;
            ConfigUtil.scheduleConfigSave();
        }
    }

    @Getter @Setter @ConfigEntry(id = "enabled")
    private boolean enabled;
    @Getter
    private static final double COIN_LIMIT = 15_000_000_000d;

    @Getter @ConfigEntry(id = "orderLimitEntries")
    private final List<OrderLimitEntry> orderLimitEntries;

    private double getTotalOrderedCoins() {
        return orderLimitEntries.stream().mapToDouble(OrderLimitEntry::price).sum();
    }

    public OrderLimitVisual(boolean enabled) {
        this.enabled = enabled;
        this.orderLimitEntries = new ArrayList<>();
    }


    @RunOnInit
    public static void registerBazaarOpen() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            ScreenInfo screenInfo = ScreenInfo.getCurrentScreenInfo();
            if(!screenInfo.inBazaar())
                return;
            BUConfig.get().feature.orderLimitVisual.removeOldEntries();
        });
    }

    public void removeOldEntries() {
        orderLimitEntries.stream().filter((entry) -> entry.time().isBefore(TimeUtil.LAST_BAZAAR_LIMIT_RESET_TIME)).toList()
                .forEach(orderLimitEntries::remove);
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

    public void addOrderToLimit(double price) {
        if(price > Integer.MAX_VALUE){
            price = Integer.MAX_VALUE; // hypixel doesnt count coins over the integer limit
        }
        orderLimitEntries.add(new OrderLimitEntry(price, ZonedDateTime.now()));
    }

    @RegisterWidget
    public static List<ClickableWidget> getWidget() {
        OrderLimitVisual orderLimitVisual = BUConfig.get().feature.orderLimitVisual;
        ScreenInfo screenInfo = ScreenInfo.getCurrentScreenInfo();
        boolean isTargetScreen = screenInfo.inMenu(ScreenInfo.BazaarMenuType.BAZAAR_MAIN_PAGE);

        if (!orderLimitVisual.isEnabled() || !isTargetScreen || !(MinecraftClient.getInstance().currentScreen instanceof AccessorHandledScreen screen))
            return Collections.emptyList();

        String screenTitle = MinecraftClient.getInstance().currentScreen.getTitle().getString();
        String orderedCoinsFormatted = formatNumberWithPrefix(orderLimitVisual.getTotalOrderedCoins());
        ItemSlotButtonWidget.ScreenWidgetDimensions dimensions = ItemSlotButtonWidget.getSafeScreenDimensions(screen,
                screenTitle);


        return List.of(createLimitWidget(dimensions, orderedCoinsFormatted), createTimeUntilResetWidget(dimensions));
    }

    private static TextDisplayWidget createTimeUntilResetWidget(ItemSlotButtonWidget.ScreenWidgetDimensions dimensions){

        int spacing = 7;
        int limitWidgetHeight = 16;
        int textSizeX = 43;
        if(BUCompatibilityHelper.isSkyblockerLoaded())
            spacing += 21;
        int widgetX = dimensions.x() + textSizeX;
        int widgetY = dimensions.y() - spacing - limitWidgetHeight;

        ZonedDateTime nextReset = TimeUtil.getNextBazaarLimitReset();
        Duration duration = Duration.between(ZonedDateTime.now(), nextReset);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        MutableText timeUntilResetFormatted = Text.literal(String.format("%02d:%02d", hours, minutes)).formatted(Formatting.DARK_GREEN);
        MutableText timeUntilText = Text.literal("Time Until Reset: ").formatted(Formatting.GOLD);
        Text timeUntilResetText = timeUntilText.append(timeUntilResetFormatted);

        return new TextDisplayWidget(widgetX, widgetY, 30, 8, timeUntilResetText);
    }

    private static ClickableWidget createLimitWidget(ItemSlotButtonWidget.ScreenWidgetDimensions dimensions, String orderedCoinsFormatted){
        OrderLimitVisual orderLimitVisual = BUConfig.get().feature.orderLimitVisual;

        Text orderedCoinsText = orderLimitVisual.getTotalOrderedCoins() >= OrderLimitVisual.COIN_LIMIT ? Text.literal(orderedCoinsFormatted).formatted(Formatting.RED) : Text.literal(orderedCoinsFormatted).formatted(Formatting.GREEN);
        Text limitText = Text.literal("/" + formatNumberWithPrefix(OrderLimitVisual.COIN_LIMIT)).formatted(Formatting.GOLD);
        Text message = Text.literal("Bazaar Order Limit: ").formatted(Formatting.GOLD)
                .append(orderedCoinsText)
                .append(limitText);

        int textSizeX = 58;
        int textSizeY = 8;
        int spacing = 5;
        if(BUCompatibilityHelper.isSkyblockerLoaded())
            spacing += 21;
        int buttonX = dimensions.x() + textSizeX;
        int buttonY = dimensions.y() - spacing - textSizeY;
        return new TextDisplayWidget(buttonX, buttonY, textSizeX, textSizeY, message);
    }
}
