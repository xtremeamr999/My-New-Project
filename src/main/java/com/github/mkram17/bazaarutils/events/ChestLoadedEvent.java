package com.github.mkram17.bazaarutils.events;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.misc.autoregistration.RunOnInit;
import com.github.mkram17.bazaarutils.utils.GUIUtils;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.ScreenInfo;
import com.github.mkram17.bazaarutils.utils.Util;
import lombok.Getter;
import meteordevelopment.orbit.ICancellable;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Event fired when a chest/container GUI is fully loaded with all items.
 * <p>
 * This event is triggered after a chest or container screen opens and all items have finished loading.
 * The mod waits for all item slots to be populated (checking that items are not in a "Loading..." state)
 * before firing this event. This ensures that listeners can safely access all container contents.
 * </p>
 * 
 * <p>The event includes:</p>
 * <ul>
 *   <li>The container's inventory</li>
 *   <li>A list of all non-empty item stacks</li>
 *   <li>The container's display name</li>
 * </ul>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * {@code
 * @EventHandler
 * public void onChestLoaded(ChestLoadedEvent event) {
 *     String containerName = event.getContainerName();
 *     if (containerName.contains("Bazaar")) {
 *         List<ItemStack> items = event.getItemStacks();
 *         processBazaarItems(items);
 *     }
 * }
 * }
 * </pre>
 * 
 * <p><strong>Implementation Note:</strong></p>
 * The event uses a polling mechanism that checks every 40ms (up to 50 attempts / 2 seconds)
 * to determine when the GUI is fully loaded. This is necessary because Hypixel loads items
 * asynchronously with placeholder "Loading..." items initially.
 * 
 * @see Inventory
 * @see ItemStack
 */
public class ChestLoadedEvent implements ICancellable {
    /**
     * The inventory of the lower chest/container (excluding the player's inventory).
     */
    @Getter
    private Inventory lowerChestInventory;
    
    /**
     * List of all non-empty item stacks in the container.
     */
    @Getter
    private List<ItemStack> itemStacks = new ArrayList<>();
    
    /**
     * The display name of the container.
     */
    @Getter
    private String containerName;

    /**
     * Singleton instance used for event registration.
     */
    public static final ChestLoadedEvent INSTANCE = new ChestLoadedEvent();

    /**
     * Registers the screen event listener that triggers this event when chests are loaded.
     * This method is automatically called during mod initialization.
     */
    @RunOnInit
    public static void registerScreenEvent() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof GenericContainerScreen genericContainerScreen) {
                class GuiLoadedChecker implements Runnable {
                    private int attempts = 0;
                    private final int MAX_ATTEMPTS = 50; // 2 seconds timeout (50 * 40ms)
                    private final long DELAY_MS = 40;

                    @Override
                    public void run() {
                        // Ensure we are still on the same screen
                        if (client.currentScreen != genericContainerScreen) {
                            return;
                        }

                        ScreenHandler handler = genericContainerScreen.getScreenHandler();
                        if (handler instanceof GenericContainerScreenHandler containerHandler) {
                            Inventory inv = containerHandler.getInventory();
                            if (!inv.isEmpty() && !inv.getStack(inv.size() - 1).isEmpty() && !areAnyItemsLoading(inv)) {
                                // GUI is loaded, post the event
                                ChestLoadedEvent event = new ChestLoadedEvent();
                                event.lowerChestInventory = inv;
                                event.containerName = ScreenInfo.getCurrentScreenInfo().getScreenName();
                                event.itemStacks = getChestItemSlots(inv);
                                BazaarUtils.EVENT_BUS.post(event);
                            } else {
                                // GUI not loaded, retry
                                attempts++;
                                if (attempts < MAX_ATTEMPTS) {
                                    // Schedule to run again after a delay
                                    CompletableFuture.runAsync(() -> {
                                        try {
                                            Thread.sleep(DELAY_MS);
                                            client.execute(this);
                                        } catch (InterruptedException e) {
                                            Thread.currentThread().interrupt();
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
                // Start the check on the client thread
                client.execute(new GuiLoadedChecker());
            }
        });
    }

    private static List<ItemStack> getChestItemSlots(Inventory inventory) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    private static boolean areAnyItemsLoading(Inventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack item = inventory.getStack(i);
            if (isItemLoading(item)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isItemLoading(ItemStack item) {
        if(item.isEmpty())
            return false;
        return item.getComponents().stream().anyMatch(component -> component.toString().contains("Loading"));
    }

    @Override
    public void setCancelled(boolean b) {

    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}