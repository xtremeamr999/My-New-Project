package com.github.mkram17.bazaarutils.events;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.utils.GUIUtils;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
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

public class ChestLoadedEvent implements ICancellable, BUListener {
    @Getter
    private Inventory lowerChestInventory;
    @Getter
    private List<ItemStack> itemStacks = new ArrayList<>();
    @Getter
    private String containerName;

    public static final ChestLoadedEvent INSTANCE = new ChestLoadedEvent();

    @Override
    public void subscribe(){
        registerScreenEvent();
    }

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
                            if (inv.size() > 0 && !inv.getStack(inv.size() - 1).isEmpty() && !isItemLoading(inv)) {
                                // GUI is loaded, post the event
                                ChestLoadedEvent event = new ChestLoadedEvent();
                                event.lowerChestInventory = inv;
                                event.containerName = GUIUtils.getContainerName();
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

    private static boolean isItemLoading(Inventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack item = inventory.getStack(i);
            if (item.isEmpty()) continue;

            Text customName = item.get(DataComponentTypes.CUSTOM_NAME);
            if (customName != null) {
                String displayName = Util.removeFormatting(customName.getString());
                if (displayName.contains("Loading")) {
                    PlayerActionUtil.notifyAll("Loading item...", Util.notificationTypes.GUI);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setCancelled(boolean b) {

    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}