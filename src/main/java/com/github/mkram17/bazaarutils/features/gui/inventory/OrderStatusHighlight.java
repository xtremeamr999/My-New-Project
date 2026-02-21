package com.github.mkram17.bazaarutils.features.gui.inventory;

import com.github.mkram17.bazaarutils.config.features.DeveloperConfig;
import com.github.mkram17.bazaarutils.config.features.gui.InventoryConfig;
import com.github.mkram17.bazaarutils.events.listener.BUListener;
import com.github.mkram17.bazaarutils.features.util.BUToggleableFeature;
import com.github.mkram17.bazaarutils.misc.SlotHighlightCache;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.*;
import com.github.mkram17.bazaarutils.utils.Util;
import com.github.mkram17.bazaarutils.utils.bazaar.market.price.PricingPosition;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption;
import lombok.Getter;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.List;

//drawing done in MixinHandledScreen
@ConfigObject
public class OrderStatusHighlight extends BUListener implements BUToggleableFeature {
    public static final Identifier IDENTIFIER = Identifier.tryParse("bazaarutils", "orderstatushighlight/background");

    @Getter
    @ConfigEntry(
            id = "enabled",
            translation = "bazaarutils.config.inventory.order_status_highlight.enabled.value"
    )
    public boolean enabled;

    @ConfigEntry(
            id = "competitive_color",
            translation = "bazaarutils.config.inventory.order_status_highlight.competitive_color.value"
    )
    @Comment(
            value = "The color to highlight orders which are the best offer to the market",
            translation = "bazaarutils.config.inventory.order_status_highlight.competitive_color.description"
    )
    @ConfigOption.Color(alpha = true)
    public int competitiveColor;

    @ConfigEntry(
            id = "matched_color",
            translation = "bazaarutils.config.inventory.order_status_highlight.matched_color.value"
    )
    @Comment(
            value = "The color to highlight orders which match the market price",
            translation = "bazaarutils.config.inventory.order_status_highlight.matched_color.description"
    )
    @ConfigOption.Color(alpha = true)
    public int matchedColor;

    @ConfigEntry(
            id = "outbid_color",
            translation = "bazaarutils.config.inventory.order_status_highlight.outbid_color.value"
    )
    @Comment(
            value = "The color to highlight orders which are below the market price",
            translation = "bazaarutils.config.inventory.order_status_highlight.outbid_color.description"
    )
    @ConfigOption.Color(alpha = true)
    public int outbidColor;

    public OrderStatusHighlight(boolean enabled, int competitiveColor, int matchedColor, int outbidColor) {
        this.enabled = enabled;
        this.competitiveColor = competitiveColor;
        this.matchedColor = matchedColor;
        this.outbidColor = outbidColor;
    }

    public Order getHighlightedOrder(int slotIndex) {
        var order = OrderInfoUtil.getUserOrderFromIndex(slotIndex);

        return order.filter(
                bazaarOrder -> bazaarOrder.getStatus() != null && bazaarOrder.getStatus() == OrderStatus.SET)
                .orElse(null);
    }

    @Override
    protected void registerFabricEvents() {
        super.subscribeToMeteorEventBus = false;
        registerTooltipListener();
    }

    public void updateHighlightCache(List<ItemStack> itemStacks) {
        if (!enabled) {
            return;
        }

        for (ItemStack stack : itemStacks) {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.player == null || !(client.currentScreen instanceof HandledScreen<?> handledScreen)) {
                continue;
            }

            int index = -1;

            for (Slot slot : handledScreen.getScreenHandler().slots) {
                if (!slot.hasStack() || !slot.getStack().equals(stack)) {
                    continue;
                }

                index = slot.getIndex();
            }

            if (index == -1) {
                continue;
            }

            SlotHighlightCache.orderStatusHighlightCache.computeIfAbsent(index, this::getHighlightColorFromIndex);
        }
    }

    private Integer getHighlightColorFromIndex(int index) {
        Order order = getHighlightedOrder(index);

        if (order == null) {
            return null;
        }

        PricingPosition pricingPosition = order.getPricingPosition();

        if (pricingPosition == null) {
            return null;
        }

        return getArgbFromOutbidStatus(pricingPosition);
    }

    //maybe could be split into separate methods, but this is fine for now
    private void registerTooltipListener() {
        ItemTooltipCallback.EVENT.register((ItemStack stack, net.minecraft.item.Item.TooltipContext context, TooltipType type, List<Text> lines) -> {
            if (!enabled) {
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();

            if (!(client.currentScreen instanceof HandledScreen<?> handledScreen)) {
                return;
            }

            int index = -1;

            for (Slot slot : handledScreen.getScreenHandler().slots) {
                if (!slot.hasStack() || !(slot.getStack().equals(stack))) {
                    continue;
                }

                index = slot.getIndex();
            }

            if (!SlotHighlightCache.orderStatusHighlightCache.containsKey(index)) {
                return;
            }

            Order order = getHighlightedOrder(index);

            if (order == null) {
                return;
            }

            PricingPosition pricingPosition = order.getPricingPosition();

            if (pricingPosition == null) {
                return;
            }

            switch (pricingPosition) {
                case COMPETITIVE:
                    lines.add(1, Text.literal("COMPETITIVE")
                            .setStyle(Style.EMPTY
                                    .withColor(TextColor.fromRgb(competitiveColor))
                                    .withBold(true)));
                    break;

                case MATCHED:
                    lines.add(1, Text.literal("MATCHED")
                            .setStyle(Style.EMPTY
                                    .withColor(TextColor.fromRgb(matchedColor))
                                    .withBold(true)));
                    break;

                case OUTBID:
                    lines.add(1, Text.literal("OUTBID")
                            .setStyle(Style.EMPTY
                                    .withColor(TextColor.fromRgb(outbidColor))
                                    .withBold(true)));

                    lines.add(2, Text.literal("Market Price: " +
                                    Util.getPrettyString(order.getMarketPrice(order.getOrderType())))
                            .setStyle(Style.EMPTY
                                    .withColor(TextColor.fromRgb(outbidColor))));
                    break;
            }
            if (DeveloperConfig.enabled) {
                var sellPrice = order.getMarketPrice(OrderType.BUY);
                var buyPrice = order.getMarketPrice(OrderType.SELL);

                lines.add(Text.literal("[BU] Buy: " + Util.getPrettyString(sellPrice) + " coins"));
                lines.add(Text.literal("[BU] Sell: " + Util.getPrettyString(buyPrice) + " coins"));
            }
        });
    }

    private int getArgbFromOutbidStatus(PricingPosition pricingPosition) {
        return switch (pricingPosition) {
            case COMPETITIVE -> competitiveColor;
            case MATCHED -> matchedColor;
            case OUTBID -> outbidColor;
        };
    }
}
