package com.github.mkram17.bazaarutils.features;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.config.BUConfigGui;
import com.github.mkram17.bazaarutils.events.BUListener;
import com.github.mkram17.bazaarutils.events.ChestLoadedEvent;
import com.github.mkram17.bazaarutils.features.restrictsell.RestrictSell;
import com.github.mkram17.bazaarutils.features.restrictsell.RestrictSellControl;
import com.github.mkram17.bazaarutils.misc.BUCompatibilityHelper;
import com.github.mkram17.bazaarutils.misc.widgets.ItemSlotButtonWidget;
import com.github.mkram17.bazaarutils.misc.widgets.TextDisplayWidget;
import com.github.mkram17.bazaarutils.mixin.AccessorHandledScreen;
import com.github.mkram17.bazaarutils.utils.GUIUtils;

import com.github.mkram17.bazaarutils.utils.TimeUtil;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.SliderControllerBuilder;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class OrderLimit implements BUListener {
    @Getter @Setter
    private boolean enabled;
    @Getter @Setter
    private double coinLimit;

    @Getter
    private final List<OrderLimitEntry> orderLimitEntries;

    private double getTotalOrderedCoins() {
        return orderLimitEntries.stream().mapToDouble(OrderLimitEntry::price).sum();
    }

    public OrderLimit(boolean enabled, double coinLimit) {
        this.enabled = enabled;
        this.coinLimit = coinLimit;
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
            getTotalOrderedCoins();
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

    public void addOrderToLimit(double price) {
        orderLimitEntries.add(new OrderLimitEntry(price, ZonedDateTime.now()));
    }

    public static List<ClickableWidget> getWidget() {
        OrderLimit orderLimit = BUConfig.get().orderLimit;
        boolean isTargetScreen = GUIUtils.inBazaar();
        if (!orderLimit.isEnabled() || !isTargetScreen || !(MinecraftClient.getInstance().currentScreen instanceof AccessorHandledScreen screen))
            return Collections.emptyList();

        String screenTitle = MinecraftClient.getInstance().currentScreen.getTitle().getString();
        String orderedCoinsFormatted = formatNumberWithPrefix(orderLimit.getTotalOrderedCoins());

        Text orderedCoinsText = orderLimit.getTotalOrderedCoins() > orderLimit.getCoinLimit() ? Text.literal(orderedCoinsFormatted).formatted(Formatting.RED) : Text.literal(orderedCoinsFormatted).formatted(Formatting.GREEN);
        Text limitText = Text.literal("/" + formatNumberWithPrefix(orderLimit.getCoinLimit())).formatted(Formatting.GOLD);
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
        init();
    }

    public record OrderLimitEntry(@Getter double price, @Getter ZonedDateTime time) {
            public OrderLimitEntry(double price, ZonedDateTime time) {
                this.price = price;
                this.time = time;
                BUConfig.HANDLER.save();
            }
        }

    public OptionGroup buildOrderLimitGroup() {
        OptionGroup.Builder restrictSellGroupBuilder = OptionGroup.createBuilder()
                .name(Text.literal("Order Limit"))
                .description(OptionDescription.of(Text.literal("Shows you how close you are to the coin order limit for the bazaar. Resets at 12am GMT.")));

        buildOptions(restrictSellGroupBuilder);

        return restrictSellGroupBuilder.build();
    }
    private void buildOptions(OptionGroup.Builder builder){
            builder.option(createEnabledOption());
            builder.option(createLimitOption());
    }


    private Option<Boolean> createEnabledOption() {
        return Option.<Boolean>createBuilder()
                .name(Text.literal("Show Bazaar Order Limit"))
                .description(OptionDescription.of(Text.literal("Shows you how close you are to the coin order limit for the bazaar at the top of the bazaar. Resets at 12am GMT.")))
                .binding(false,
                        this::isEnabled,
                        this::setEnabled)
                .controller(BUConfigGui::createBooleanController)
                .build();
    }

    private Option<Double> createLimitOption() {
        return Option.<Double>createBuilder()
                .name(Text.literal("Order Limit"))
                .description(OptionDescription.of(Text.literal("The order limit you want to display.")))
                .binding(coinLimit,
                        this::getCoinLimit,
                        this::setCoinLimit)
                .controller(DoubleFieldControllerBuilder::create)
                .build();
    }
}
