package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.events.BUListener;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderData;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

//drawing done in MixinHandledScreen
@NoArgsConstructor
public class OrderStatusHighlight implements BUListener {
    @Getter @Setter
    private boolean enabled = true;
    private static final Set<Integer> redHighlightIndices = new HashSet<>();
    private static final Set<Integer> greenHighlightIndices = new HashSet<>();
    private static final Set<Integer> yellowHighlightIndices = new HashSet<>();
    public static final Identifier IDENTIFIER = Identifier.tryParse("bazaarutils", "orderstatushighlight/background_test");
    public static final float BACKGROUND_TRANSPARENCY = 0.8f;

    public static void addOutdatedSlotIndex(int slotIndex) {
        redHighlightIndices.add(slotIndex);
    }
    public static void addCompetitiveSlotIndex(int slotIndex) {
        greenHighlightIndices.add(slotIndex);
    }
    public static void addMatchedSlotIndex(int slotIndex) {
        yellowHighlightIndices.add(slotIndex);
    }

    public static boolean shouldHighlightOutdated(int slotIndex) {
        return redHighlightIndices.contains(slotIndex);
    }
    public static boolean shouldHighlightCompetitive(int slotIndex) {
        return greenHighlightIndices.contains(slotIndex);
    }
    public static boolean shouldHighlightMatched(int slotIndex) {
        return yellowHighlightIndices.contains(slotIndex);
    }
    private static OrderData.statuses getHighlightType(int slotIndex){
        if (shouldHighlightOutdated(slotIndex)) {
            return OrderData.statuses.OUTDATED;
        } else if (shouldHighlightCompetitive(slotIndex)) {
            return OrderData.statuses.COMPETITIVE;
        } else if (shouldHighlightMatched(slotIndex)) {
            return OrderData.statuses.MATCHED;
        }
        return null;
    }

    public static void clearHighlightedSlots() {
        redHighlightIndices.clear();
        greenHighlightIndices.clear();
        yellowHighlightIndices.clear();
    }

    private void registerScreenRenderEvents() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            // Clear highlights when any HandledScreen initializes.
            if (screen instanceof HandledScreen) {
                OrderStatusHighlight.clearHighlightedSlots();
            }
        });
    }

    @Override
    public void subscribe() {
        registerScreenRenderEvents();
        registerTooltipListener();
    }

    public Option<Boolean> createOption() {
        return Option.<Boolean>createBuilder()
                .name(Text.literal("Order Status Highlight"))
                .description(OptionDescription.of(Text.literal("Puts a red border around orders that are outdated and a green border around orders that are not outdated.")))
                .binding(false,
                        this::isEnabled,
                        this::setEnabled)
                .controller(BUConfig::createBooleanController)
                .build();
    }

    private void registerTooltipListener() {
        ItemTooltipCallback.EVENT.register((ItemStack stack, net.minecraft.item.Item.TooltipContext context, TooltipType type, List<Text> lines) -> {
            if (!enabled) return;
            if (stack == null || stack.isEmpty() || stack.getItem().getName().getString().contains("GLASS_PANE")) {
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || !(client.currentScreen instanceof HandledScreen<?> handledScreen)) {
                return;
            }
            int index = -1;
            for (Slot slot : handledScreen.getScreenHandler().slots) {
                if (!(slot.hasStack() && slot.getStack() == stack))
                    continue;
                index = slot.id;
            }
            if(index == -1)
                return;

            OrderData.statuses highlightType = getHighlightType(index);
            if (highlightType == null) {
                return;
            }

            switch (highlightType) {
                case OUTDATED:
                    lines.add(1, Text.literal("OUTDATED").formatted(Formatting.RED, Formatting.BOLD));
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
