package com.github.mkram17.bazaarutils.features;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.config.BUConfigGui;
import com.github.mkram17.bazaarutils.events.handlers.BUListener;
import com.github.mkram17.bazaarutils.misc.BUCompatibilityHelper;
import com.github.mkram17.bazaarutils.misc.autoregistration.RegisterWidget;
import com.github.mkram17.bazaarutils.misc.autoregistration.RunOnInit;
import com.github.mkram17.bazaarutils.misc.widgets.ItemSlotButtonWidget;
import com.github.mkram17.bazaarutils.misc.widgets.TextDisplayWidget;
import com.github.mkram17.bazaarutils.mixin.AccessorHandledScreen;

import com.github.mkram17.bazaarutils.utils.ScreenInfo;
import com.github.mkram17.bazaarutils.utils.TimeUtil;
import com.github.mkram17.bazaarutils.utils.Util;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class OrderLimit implements BUListener, BUToggleableFeature {
    @Getter @Setter
    private boolean enabled;
    @Getter
    private static final double COIN_LIMIT = 15_000_000_000d;

    @Getter
    private final List<OrderLimitEntry> orderLimitEntries;

    private double getTotalOrderedCoins() {
        return orderLimitEntries.stream().mapToDouble(OrderLimitEntry::price).sum();
    }

    public OrderLimit(boolean enabled) {
        this.enabled = enabled;
        this.orderLimitEntries = new ArrayList<>();
    }


    @RunOnInit
    public static void registerBazaarOpen() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            ScreenInfo screenInfo = ScreenInfo.getCurrentScreenInfo();
            if(screenInfo == null || !screenInfo.inBazaar())
                return;
            BUConfig.get().orderLimit.removeOldEntries();
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
        OrderLimit orderLimit = BUConfig.get().orderLimit;
        ScreenInfo screenInfo = ScreenInfo.getCurrentScreenInfo();
        boolean isTargetScreen = screenInfo.inBazaar();
        if (!orderLimit.isEnabled() || !isTargetScreen || !(MinecraftClient.getInstance().currentScreen instanceof AccessorHandledScreen screen))
            return Collections.emptyList();

        String screenTitle = MinecraftClient.getInstance().currentScreen.getTitle().getString();
        String orderedCoinsFormatted = formatNumberWithPrefix(orderLimit.getTotalOrderedCoins());

        Text orderedCoinsText = orderLimit.getTotalOrderedCoins() >= orderLimit.getCOIN_LIMIT() ? Text.literal(orderedCoinsFormatted).formatted(Formatting.RED) : Text.literal(orderedCoinsFormatted).formatted(Formatting.GREEN);
        Text limitText = Text.literal("/" + formatNumberWithPrefix(orderLimit.getCOIN_LIMIT())).formatted(Formatting.GOLD);
        Text message = Text.literal("Bazaar Order Limit: ").formatted(Formatting.GOLD)
                .append(orderedCoinsText)
                .append(limitText);

        ItemSlotButtonWidget.ScreenWidgetDimensions dimensions = ItemSlotButtonWidget.getSafeScreenDimensions(screen,
                screenTitle);

        int textSizeX = 58;
        int textSizeY = 8;
        int spacing = 5;
        if(BUCompatibilityHelper.isSkyblockerLoaded())
            spacing += 21;
        int buttonX = dimensions.x() + textSizeX;
        int buttonY = dimensions.y() - spacing - textSizeY;
        TextDisplayWidget widget = new TextDisplayWidget(buttonX, buttonY, textSizeX, textSizeY, message);
        return Collections.singletonList(widget);
    }

    @Override
    public void subscribe() {
        BazaarUtils.EVENT_BUS.subscribe(this);
    }

    public record OrderLimitEntry(@Getter double price, @Getter ZonedDateTime time) {
            public OrderLimitEntry(double price, ZonedDateTime time) {
                this.price = price;
                this.time = time;
                Util.scheduleConfigSave();
            }
        }

    public Option<Boolean> createOption() {
        return BUToggleableFeature.createOptionHelper("Show Bazaar Order Limit",
                "Shows you how close you are to the coin order limit for the bazaar at the top of the bazaar. Resets at 12am GMT.",
                false,
                this::isEnabled,
                this::setEnabled);
    }
}
