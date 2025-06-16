package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.events.BUListener;
import com.github.mkram17.bazaarutils.mixin.AccessorHandledScreen;
import com.github.mkram17.bazaarutils.utils.Util;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
public class OrderStatusHighlight implements BUListener {
    @Getter @Setter
    private boolean enabled = true;
    private static final int OUTDATED_BORDER_COLOR = Color.RED.getRGB();
    private static final int COMPETITIVE_BORDER_COLOR = Color.GREEN.getRGB();
    private static final Set<Integer> redHighlightIndices = new HashSet<>();
    private static final Set<Integer> greenHighlightIndices = new HashSet<>();

    public static void addOutdatedSlotIndex(int slotIndex) {
        redHighlightIndices.add(slotIndex);
    }
    public static void addCompetitiveSlotIndex(int slotIndex) {
        greenHighlightIndices.add(slotIndex);
    }


    public static void removeSlotIndex(int slotIndex) {
        if(redHighlightIndices.contains(slotIndex)) {
            redHighlightIndices.remove(slotIndex);
        } else {
            greenHighlightIndices.remove(slotIndex);
        }
    }

    public static void clearHighlightedSlots() {
        redHighlightIndices.clear();
        greenHighlightIndices.clear();
    }

    public static void renderBorders(DrawContext drawContext, Screen screen) {
        if (!(screen instanceof HandledScreen<?> handledScreen)) {
            return;
        }

        if (!(handledScreen instanceof AccessorHandledScreen accessorHandledScreen)) {
            Util.notifyError("Failed to access HandledScreen properties for border rendering.", null);
            return;
        }

        //must remove empty stacks because ItemUpdater only looks through ItemStacks
        DefaultedList<Slot> slots = getSlots(handledScreen);


        drawSlotBorders(drawContext, accessorHandledScreen, slots, redHighlightIndices, OUTDATED_BORDER_COLOR);
        drawSlotBorders(drawContext, accessorHandledScreen, slots, greenHighlightIndices, COMPETITIVE_BORDER_COLOR);
    }

    private static void drawSlotBorders(DrawContext drawContext, AccessorHandledScreen accessorHandledScreen, DefaultedList<Slot> slots, Set<Integer> redHighlightIndices, int borderColor) {
        for (Integer slotIndex : redHighlightIndices) {
            if (slotIndex >= 0 && slotIndex < slots.size()) {
                Slot slot = slots.get(slotIndex);
                if (slot.hasStack()) {
                    int slotX = accessorHandledScreen.getX() + slot.x;
                    int slotY = accessorHandledScreen.getY() + slot.y;

                    drawContext.drawBorder(slotX - 1, slotY - 1, 16 + 1, 16 + 1, borderColor);
                }
            }
        }
    }

    private void registerScreenRenderEvents() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {

            // Clear highlights when any HandledScreen initializes.
            if (screen instanceof HandledScreen) {
                OrderStatusHighlight.clearHighlightedSlots();
            }

            ScreenEvents.afterRender(screen).register((currentScreen, drawContext, mouseX, mouseY, tickDelta) -> {
                if (enabled && currentScreen == screen && BazaarUtils.gui.inOrderScreen()) {
                    renderBorders(drawContext, currentScreen);
                }
            });

            // Clear highlights when a HandledScreen is removed (closed)
            ScreenEvents.remove(screen).register((removedScreen) -> {
                if (screen instanceof HandledScreen && removedScreen == screen) {
                    OrderStatusHighlight.clearHighlightedSlots();
                }
            });
        });
    }

    @Override
    public void subscribe() {
        registerScreenRenderEvents();
    }

    private static DefaultedList<Slot> getSlots(HandledScreen<?> screen) {
        DefaultedList<Slot> slots = DefaultedList.of();
        for(Slot slot : screen.getScreenHandler().slots) {
            if (slot != null && slot.hasStack()) {
                slots.add(slot);
            }
        }
        return slots;
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
}
