package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.events.BUListener;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

//drawing done in MixinHandledScreen
@NoArgsConstructor
public class OrderStatusHighlight implements BUListener {
    @Getter @Setter
    private boolean enabled = true;
    private static final Set<Integer> redHighlightIndices = new HashSet<>();
    private static final Set<Integer> greenHighlightIndices = new HashSet<>();
    public static final Identifier IDENTIFIER = Identifier.tryParse("bazaarutils", "orderstatushighlight/item_background_square");

    public static void addOutdatedSlotIndex(int slotIndex) {
        redHighlightIndices.add(slotIndex);
    }
    public static void addCompetitiveSlotIndex(int slotIndex) {
        greenHighlightIndices.add(slotIndex);
    }

    public static boolean shouldHighlightOutdated(int slotIndex) {
        return redHighlightIndices.contains(slotIndex);
    }
    public static boolean shouldHighlightCompetitive(int slotIndex) {
        return greenHighlightIndices.contains(slotIndex);
    }

    public static void clearHighlightedSlots() {
        redHighlightIndices.clear();
        greenHighlightIndices.clear();
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
