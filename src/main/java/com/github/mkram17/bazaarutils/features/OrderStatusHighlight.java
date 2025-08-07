package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.config.BUConfigGui;
import com.github.mkram17.bazaarutils.events.handlers.BUListener;
import com.github.mkram17.bazaarutils.misc.orderinfo.BazaarOrder;
import com.github.mkram17.bazaarutils.misc.orderinfo.PriceInfo;
import com.github.mkram17.bazaarutils.utils.ScreenInfo;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

//drawing done in MixinHandledScreen
public class OrderStatusHighlight implements BUListener {
    @Getter @Setter
    private boolean enabled;
    public static final Identifier IDENTIFIER = Identifier.tryParse("bazaarutils", "orderstatushighlight/background");
    public static final float BACKGROUND_TRANSPARENCY = 0.9f;

    public OrderStatusHighlight(boolean enabled){
        this.enabled = enabled;
    }

    private static List<BazaarOrder> getHighlightedOrders() {
        return BUConfig.get().userOrders.stream()
                .filter(order -> order.getItemInfo() != null
                        && order.getItemInfo().slotIndex() != null)
                .toList();
    }

    public static BazaarOrder getHighlightedOrder(int slotIndex) {
        return getHighlightedOrders().stream()
                .filter(order -> order.getItemInfo().slotIndex() == slotIndex)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void subscribe() {
//        registerScreenRenderEvents();
        registerTooltipListener();
    }

    public Option<Boolean> createOption() {
        return Option.<Boolean>createBuilder()
                .name(Text.literal("Order Status Highlight"))
                .description(OptionDescription.of(Text.literal("Adds a colored background and tooltip for orders that are competitive, matched or outdated in the orders gui inside the bazaar. For outdated orders, also adds the market price in the tooltip.")))
                .binding(false,
                        this::isEnabled,
                        this::setEnabled)
                .controller(BUConfigGui::createBooleanController)
                .build();
    }

    //maybe could be split into separate methods, but this is fine for now
    private void registerTooltipListener() {
        ItemTooltipCallback.EVENT.register((ItemStack stack, net.minecraft.item.Item.TooltipContext context, TooltipType type, List<Text> lines) -> {
            if (!enabled) return;
            ScreenInfo screenInfo = ScreenInfo.getCurrentScreenInfo();
            if (stack == null || stack.isEmpty() || stack.getItem().getName().getString().contains("GLASS_PANE") || !screenInfo.inMenu(ScreenInfo.BazaarMenuType.ORDER_SCREEN)) {
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || !(client.currentScreen instanceof HandledScreen<?> handledScreen)) {
                return;
            }

            for (Text line : lines) {
                String lineText = line.getString();
                if (lineText.contains("FILLED") || lineText.contains("OUTDATED") ||
                        lineText.contains("COMPETITIVE") || lineText.contains("MATCHED")) {
                    // the tooltip is already present, skip processing
                    return;
                }
            }

            int index = -1;
            for (Slot slot : handledScreen.getScreenHandler().slots) {
                if (!slot.hasStack() || !(slot.getStack() == stack))
                    continue;
                index = slot.getIndex();
            }

            if(index == -1)
                return;

            BazaarOrder order = getHighlightedOrder(index);
            if (order == null) {
                return;
            }

            switch (order.getOutdatedStatus()) {
                case OUTDATED:
                    lines.add(1, Text.literal("OUTDATED").formatted(Formatting.RED, Formatting.BOLD));
                    lines.add(2, Text.literal("Market Price: " + PriceInfo.getPrettyString(order.getMarketPrice())).formatted(Formatting.RED));
                    break;
                case COMPETITIVE:
                    lines.add(1, Text.literal("COMPETITIVE").formatted(Formatting.GREEN, Formatting.BOLD));
                    break;
                case MATCHED:
                    lines.add(1, Text.literal("MATCHED").formatted(Formatting.YELLOW, Formatting.BOLD));
                    break;
            }
        });
    }
}
