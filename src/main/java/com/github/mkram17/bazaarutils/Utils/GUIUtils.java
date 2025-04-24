package com.github.mkram17.bazaarutils.Utils;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.Events.ChestLoadedEvent;
import com.github.mkram17.bazaarutils.Events.SignOpenEvent;
import com.github.mkram17.bazaarutils.features.Bookmark;
import com.github.mkram17.bazaarutils.mixin.AccessorSignEditScreen;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

//TODO make inBazaar() work all the time
public class GUIUtils {
    public boolean wasLastChestFlip(){
        return inFlipGui;
    }

    public boolean inBuyOrderScreen(){
        if(containerName == null) return false;
        return containerName.contains("How many do you want?");
    }
    public boolean inInstaBuy(){
        if(containerName == null) return false;
        return containerName.contains("Instant Buy");
    }
    public boolean inBuyOrders(){
        if(containerName == null) return false;
        return containerName.contains("Co-op Bazaar Orders");
    }

    public boolean inBazaar(){
//        return false;
        if(containerName == null) return false;
        return inBuyOrderScreen() || inFlipGui || inInstaBuy() || containerName.contains("Bazaar") || inBuyOrders();
    }

    public boolean inAnyItemScreen(){
        if(containerName == null || containerName.contains("Bazaar")) return false;
        return containerName.contains("➜")
                || inBuyOrderScreen()
                || inInstaBuy();
    }
    private GenericContainerScreen chestScreen;
    @Getter
    private String containerName;
    @Getter
    @Setter
    private guiTypes guiType;
    private  List<ItemStack> itemStacks = new ArrayList<>();
    public boolean inFlipGui;
    @Getter @Setter
    private static Inventory lowerChestInventory;
    @Getter @Setter
    private Bookmark currentBookmark;
    @Getter @Setter
    private String previousScreenName;

    public enum guiTypes {CHEST, SIGN}
    public void register(){

    }
    public void registerScreenEvent(){
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            BazaarUtils.gui = this;
            lowerChestInventory= null;
            if (screen instanceof GenericContainerScreen genericContainerScreen) {
                containerName = Util.removeFormatting(genericContainerScreen.getTitle().getString());
                Util.notifyAll("Container Name: " + containerName, Util.notificationTypes.GUI);
            }
        });

        ScreenEvents.BEFORE_INIT.register((client, screen, width, height) -> {
            BazaarUtils.gui.previousScreenName = BazaarUtils.gui.containerName;
        });
    }
    @EventHandler(priority = EventPriority.HIGH)
    private void loadSign(SignOpenEvent e){
        guiType = guiType.SIGN;
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onChestLoaded(ChestLoadedEvent e){
        guiType = guiType.CHEST;
        itemStacks = e.getItemStacks();
        currentBookmark = null;

        if(BazaarUtils.gui.inBuyOrderScreen() || BazaarUtils.gui.inInstaBuy() || BazaarUtils.gui.inAnyItemScreen()){
            String name = Bookmark.findName(e);
            if(Bookmark.isBookmarked(name)){
                currentBookmark = Bookmark.findMatchingBookmark(name);
                BazaarUtils.eventBus.subscribe(currentBookmark);
            } else
                currentBookmark = new Bookmark(name, Items.BARRIER.getDefaultStack());
        }
    }

    public static void closeHandledScreen() {
        try {
            Util.notifyAll("Closing gui", Util.notificationTypes.GUI);
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) {
                Util.notifyAll("Client is null", Util.notificationTypes.ERROR);
                return;
            }

            client.execute(() -> {
                var player = client.player;
                if (player != null) {
                    player.closeHandledScreen();
                } else {
                    Util.notifyAll("Player is null", Util.notificationTypes.ERROR);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Util.notifyAll("Error closing gui", Util.notificationTypes.ERROR);
        }
    }

    public static void closeSign(){
        try {
            Util.notifyAll("Closing sign", Util.notificationTypes.GUI);
            MinecraftClient mcclient = MinecraftClient.getInstance();
            if (mcclient != null && mcclient.currentScreen instanceof AbstractSignEditScreen signEditScreen) {
                mcclient.execute(signEditScreen::close);
            } else {
                Util.notifyAll("Error closing sign: client was null or not in a sign", Util.notificationTypes.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Util.notifyAll("Unknown error while closing sign", Util.notificationTypes.ERROR);
        }
    }



    public static void setSignText(String text, boolean closeAfter) {
        final int MAX_ATTEMPTS = 5;
        final long DELAY_MS = 250;

        CompletableFuture.runAsync(() -> {
            for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client != null && client.currentScreen instanceof SignEditScreen) {
                    final SignEditScreen screen = (SignEditScreen) client.currentScreen;
                    client.execute(() -> {
                        try {
                            AccessorSignEditScreen signScreen = (AccessorSignEditScreen) screen;
                            String[] lines = text.split("\n", 4);
                            int originalRow = signScreen.getCurrentRow();

                            for (int i = 0; i < 4; i++) {
                                String line = i < lines.length ? lines[i] : "";
                                signScreen.setCurrentRow(i);
                                signScreen.callSetCurrentRowMessage(line);
                            }
                            signScreen.setCurrentRow(originalRow);
                        } catch (Exception e) {
                            Util.notifyAll("Error executing sign text update: " + e.getMessage(), Util.notificationTypes.ERROR);
                            e.printStackTrace();
                        }
                    });
                    if (closeAfter)
                        closeSign();
                    return;
                } else {
                    if (attempt < MAX_ATTEMPTS - 1) {
                        try {
                            Thread.sleep(DELAY_MS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            MinecraftClient finalClient = MinecraftClient.getInstance();
                            if (finalClient != null) {
                                finalClient.execute(() -> Util.notifyAll("Sign text setting interrupted", Util.notificationTypes.ERROR));
                            }
                            return;
                        }
                    }
                }
            }

            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                client.execute(() -> Util.notifyAll("Error setting sign text: client was null or not in a sign after " + MAX_ATTEMPTS + " attempts", Util.notificationTypes.ERROR));
            } else {
                System.err.println("Error setting sign text: Failed after " + MAX_ATTEMPTS + " attempts, client was null.");
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onLoad(ChestLoadedEvent e){
        lowerChestInventory = e.getLowerChestInventory();
        updateFlipGui();
    }

    private boolean inFlipGui() {
        if (containerName == null || lowerChestInventory == null) {
            return false;
        }

        if (!containerName.contains("Order options")) {
            return false;
        }

        ItemStack stack = lowerChestInventory.getStack(13);

        // Check if the item name contains "Cancel Order"
        String customName = stack.getName().getString();
        return !customName.contains("Cancel Order");
    }

    public void updateFlipGui(){
        if(inFlipGui()) {
            inFlipGui = true;
            Util.notifyAll("In flip gui", Util.notificationTypes.GUI);
        }
        else if(guiType == guiTypes.CHEST) {
            inFlipGui = false;
//            Util.notifyAll("Flip gui removed", Util.notificationTypes.GUI);
        }
    }
    public static void clickSlot(int slotIndex, int button) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerInteractionManager interactionManager = client.interactionManager;
        ClientPlayerEntity player = client.player;

        if (interactionManager == null || player == null) return;

        ScreenHandler screenHandler = player.currentScreenHandler;
        int syncId = screenHandler.syncId;
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(30);
                // Use the interaction manager to handle the click
                interactionManager.clickSlot(
                        syncId,       // Sync ID of the screen handler
                        slotIndex,    // Slot index to click
                        button,       // Mouse button (0 = left, 1 = right)
                        SlotActionType.PICKUP,   // Slot action type (e.g., PICKUP, QUICK_MOVE)
                        player        // The player performing the action
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void sendCommand(String command){
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.networkHandler.sendChatCommand(command);
        }
    }

}
