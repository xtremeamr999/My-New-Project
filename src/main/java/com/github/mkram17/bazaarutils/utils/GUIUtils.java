package com.github.mkram17.bazaarutils.utils;

import com.github.mkram17.bazaarutils.events.BUListener;
import com.github.mkram17.bazaarutils.events.ChestLoadedEvent;
import com.github.mkram17.bazaarutils.events.ScreenChangeEvent;
import com.github.mkram17.bazaarutils.events.SignOpenEvent;
import com.github.mkram17.bazaarutils.features.Bookmark;
import com.github.mkram17.bazaarutils.mixin.AccessorSignEditScreen;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.concurrent.CompletableFuture;

import static com.github.mkram17.bazaarutils.BazaarUtils.EVENT_BUS;

//TODO make inBazaar() work all the time
public class GUIUtils implements BUListener {
    public static final GUIUtils INSTANCE = new GUIUtils();
    @Getter @Setter
    private static guiTypes guiType;
    @Getter @Setter
    private static Inventory lowerChestInventory;
    @Getter @Setter
    private static Bookmark currentBookmark;

    @Override
    public void subscribe() {
        EVENT_BUS.subscribe(this);
        registerScreenEvent();
    }

    public enum guiTypes {CHEST, SIGN}


    public static void registerScreenEvent(){
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            lowerChestInventory = null;
            if(screen != null && ScreenInfo.getCurrentScreenInfo() == null)
                ScreenInfo.initializeScreenInfo(screen);
        });
    }

    public static ScreenHandler getHandledScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return null;
        return client.player.currentScreenHandler;
    }

    @EventHandler(priority = EventPriority.HIGH)
    private static void loadSign(SignOpenEvent e){
        guiType = guiType.SIGN;
    }

    @EventHandler(priority = EventPriority.HIGH)
    private static void onChestLoaded(ChestLoadedEvent e){
        guiType = guiType.CHEST;

        currentBookmark = null;
        if(!ScreenInfo.getCurrentScreenInfo().inAnyItemScreen())
            return;
        String name = Bookmark.findName(e);
        if (Bookmark.isBookmarked(name)) {
            currentBookmark = Bookmark.findMatchingBookmark(name);
            EVENT_BUS.subscribe(currentBookmark);
        } else
            currentBookmark = new Bookmark(name, null);
    }

    //there's some fuck ass recursion happening here from player.closeHandledScreen() and idrk why
    public static void closeHandledScreen() {
        try {
            PlayerActionUtil.notifyAll("Closing gui", Util.notificationTypes.GUI);
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) {
                Util.notifyError("Client is null", new Throwable());
                return;
            }
            if(!(client.currentScreen instanceof HandledScreen<?>))
                return;

            client.execute(GUIUtils::customCloseHandledScreen);
        } catch (Exception e) {
            e.printStackTrace();
            Util.notifyError("Error closing gui", e);
        }
    }

    private static void customCloseHandledScreen() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity player = client.player;
            if (player == null) {
                Util.notifyError("Player is null, cannot close screen", new Throwable());
                return;
            }
            player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(player.currentScreenHandler.syncId));
            client.setScreen(null);
            player.currentScreenHandler = player.playerScreenHandler;

        } catch (Exception e) {
            Util.notifyError("Error encountered while closing screen with custom method", e);
            throw new RuntimeException(e);
        }
    }

    public static void closeSign(){
        try {
            PlayerActionUtil.notifyAll("Closing sign", Util.notificationTypes.GUI);
            MinecraftClient mcclient = MinecraftClient.getInstance();
            if (mcclient != null && mcclient.currentScreen instanceof AbstractSignEditScreen signEditScreen) {
                mcclient.execute(signEditScreen::close);
            } else {
                Util.notifyError("Error closing sign: client was null or not in a sign", new Throwable());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Util.notifyError("Unknown error while closing sign", e);
        }
    }

    public static void setSignText(String text, boolean closeAfter) {
        setSignTextInternal(text, closeAfter, 5);
    }

    private static void setSignTextInternal(String text, boolean closeAfter, int attemptsLeft) {
        if (attemptsLeft <= 0) {
            Util.notifyError("Failed to set sign text: Screen not available.", new Throwable());
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            Util.notifyError("Failed to set sign text: MinecraftClient is null.", new Throwable());
            return;
        }

        client.execute(() -> {
            if (client.currentScreen instanceof SignEditScreen screen) {
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

                    if (closeAfter) {
                        closeSign();
                    }
                } catch (Exception e) {
                    Util.notifyError("Error executing sign text update", e);
                    e.printStackTrace();
                }
            } else {
                // Screen not open yet, schedule a retry
                Util.tickExecuteLater(4, () -> setSignTextInternal(text, closeAfter, attemptsLeft - 1));
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onLoad(ChestLoadedEvent e){
        lowerChestInventory = e.getLowerChestInventory();
    }

    public static void clickSlot(int slotIndex, int button) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerInteractionManager interactionManager = client.interactionManager;
        ClientPlayerEntity player = client.player;

        if (interactionManager == null || player == null) return;

        ScreenHandler screenHandler = player.currentScreenHandler;
        int syncId = screenHandler.syncId;
        Util.tickExecuteLater(1, () -> {
            interactionManager.clickSlot(
                    syncId,
                    slotIndex,
                    button,
                    SlotActionType.PICKUP,
                    player
            );
        });
    }

}
