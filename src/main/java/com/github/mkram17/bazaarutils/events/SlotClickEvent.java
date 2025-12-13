package com.github.mkram17.bazaarutils.events;

import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.ICancellable;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired when a slot is clicked in a handled screen (GUI with inventory).
 * <p>
 * This event is triggered when the player clicks on a slot in any GUI that contains an inventory,
 * such as chests, the bazaar interface, or the player's inventory. The event can be cancelled to
 * prevent the click action from being processed.
 * </p>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * {@code
 * @EventHandler
 * public void onSlotClick(SlotClickEvent event) {
 *     if (shouldPreventClick(event.slot)) {
 *         event.setCancelled(true);
 *         return;
 *     }
 *     // Process the click normally
 * }
 * }
 * </pre>
 * 
 * @see HandledScreen
 * @see Slot
 * @see SlotActionType
 */
public class SlotClickEvent implements ICancellable {
    /**
     * The screen where the slot was clicked.
     */
    @NotNull
    public final HandledScreen<?> handledScreen;
    
    /**
     * The slot that was clicked.
     */
    @NotNull
    public final Slot slot;
    
    /**
     * The index of the slot that was clicked.
     */
    public final int slotId;
    
    /**
     * The mouse button that was clicked.
     */
    public int clickedButton;
    
    /**
     * The type of click action performed.
     */
    public SlotActionType clickType;
    
    /**
     * If true, the pickblock action will be used instead of the normal click action.
     */
    public boolean usePickblockInstead = false;
    
    /**
     * Whether this event has been cancelled.
     */
    @Setter
    @Getter
    public boolean cancelled = false;

    /**
     * Creates a new SlotClickEvent.
     *
     * @param handledScreen the screen where the click occurred
     * @param slot the slot that was clicked
     * @param slotId the numeric ID of the slot
     * @param clickedButton the mouse button clicked
     * @param actionType the type of click action
     */
    public SlotClickEvent(HandledScreen<?> handledScreen, Slot slot, int slotId, int clickedButton, SlotActionType actionType) {
        this.handledScreen = handledScreen;
        this.slot = slot;
        this.slotId = slotId;
        this.clickedButton = clickedButton;
        this.clickType = actionType;
    }

}