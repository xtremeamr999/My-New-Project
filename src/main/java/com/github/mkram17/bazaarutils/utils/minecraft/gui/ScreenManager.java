package com.github.mkram17.bazaarutils.utils.minecraft.gui;

import com.github.mkram17.bazaarutils.events.ChestLoadedEvent;
import com.github.mkram17.bazaarutils.events.ScreenChangeEvent;
import com.github.mkram17.bazaarutils.events.SignOpenEvent;
import com.github.mkram17.bazaarutils.features.gui.buttons.Bookmarks;
import com.github.mkram17.bazaarutils.misc.NotificationType;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.Util;
import com.github.mkram17.bazaarutils.utils.annotations.autoregistration.RunOnInit;
import com.github.mkram17.bazaarutils.utils.bazaar.gui.BazaarScreens;
import com.github.mkram17.bazaarutils.utils.minecraft.SlotLookup;
import com.github.mkram17.bazaarutils.utils.minecraft.gui.container.ContainerManager;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.github.mkram17.bazaarutils.BazaarUtils.EVENT_BUS;

//TODO make inBazaar() work all the time
public class ScreenManager {
    @Getter
    private static final ScreenManager instance = new ScreenManager();

    @Getter @Setter
    private static ScreenKind screenKind;

    public enum ScreenKind {
        CONTAINER,
        SIGN
    }

    @RunOnInit
    public static void initialize() {
        BazaarScreens.initialize();

        EVENT_BUS.subscribe(ScreenManager.class);

        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            setCurrentScreen(screen);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private static void onScreenChange(ScreenChangeEvent event) {
        screenKind = ScreenKind.CONTAINER;

        setCurrentScreen(event.getNewScreen());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private static void onChestLoaded(ChestLoadedEvent event) {
        screenKind = ScreenKind.CONTAINER;

        ContainerManager.onChestLoaded(event);
        computeCurrentType();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private static void onSignOpened(SignOpenEvent event){
        screenKind = ScreenKind.SIGN;

        setCurrentScreen(event.getSignEditScreen());
        computeCurrentType();
    }

    private static final List<ScreenType> types = new CopyOnWriteArrayList<>();

    public static void register(ScreenType type) {
        //  should probably add a check so that the same type doesn't register more than once
        types.add(type);
    }

    @Getter
    private static ScreenType currentType;

    @Getter
    private Screen current;
    @Getter
    private Screen previous;

    private static void computeCurrentType() {
        currentType = null;

        for (ScreenType type : types) {
            try {
                if (type.test(instance.getCurrent())) {
                    currentType = type;

                    return;
                }
            } catch (Throwable ignored) {
            }
        }
    }

    /*
     * returns true if the instance is in any of the specified types of bazaar menus
     */
    public static boolean isCurrent(ScreenType... wanted) {
        if (currentType == null) {
            return false;
        }

        for (ScreenType type : wanted) {
            if (type == currentType) {
                return true;
            }
        }

        return false;
    }

    public boolean matches(ScreenType wanted) {
        return wanted.test(current);
    }

    public Optional<GenericContainerScreen> inGenericContainerScreen() {
        if (current instanceof GenericContainerScreen containerScreen) {
            return Optional.of(containerScreen);
        } else {
            return Optional.empty();
        }
    }

    public boolean inRegisteredScreenType() {
        return isCurrent(types.toArray(ScreenType[]::new));
    }

    /**
     * Gets a temporary ContainerManager object representing the previous screen.
     * Returns null if there was no previous screen.
     */
    public static ScreenManager getPreviousScreen() {
        if (instance.previous == null) {
            return null;
        }

        ScreenManager prevInfo = new ScreenManager();
        prevInfo.current = instance.previous;

        return prevInfo;
    }

    public static void setCurrentScreen(Screen screen) {
        instance.previous = instance.current;
        instance.current = screen;

        computeCurrentType();
    }

    public static <T extends ScreenHandler> Optional<T> getCurrentScreenHandler(Class<T> type) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.player == null) {
            return Optional.empty();
        }

        return type.isInstance(client.player.currentScreenHandler) ? Optional.of(type.cast(client.player.currentScreenHandler)) : Optional.empty();
    }

    public static <T extends HandledScreen<?>> Optional<T> getCurrentlyHandledScreen(Class<T> type) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.player == null) {
            return Optional.empty();
        }

        return type.isInstance(client.currentScreen) ? Optional.of(type.cast(client.currentScreen)) : Optional.empty();
    }

    public static Optional<Inventory> getScreenContainer() {
        return getCurrentScreenHandler(GenericContainerScreenHandler.class).map(GenericContainerScreenHandler::getInventory);
    }

    public static Optional<Integer> getScreenContainerSize() {
        return getScreenContainer().map(Inventory::size);
    }

    public static ItemStack getScreenItem(int chestSlot) {
        return getScreenContainer()
                .map((inventory) -> SlotLookup.getInventoryItem(inventory, chestSlot))
                .orElse(ItemStack.EMPTY);
    }

    public static Optional<Integer> getInventorySlotFromItemStack(ItemStack wanted) {
        return getScreenContainer().flatMap(inventory -> SlotLookup.getInventorySlotFromItemStack(inventory, wanted));
    }

    public static void closeHandledScreen() {
        PlayerActionUtil.notifyAll("Closing GUI", NotificationType.GUI);

        var currentScreenOpt = getCurrentlyHandledScreen(HandledScreen.class);

        if (currentScreenOpt.isEmpty()) {
            Util.notifyError("Current screen does not implement HandledScreen", new Throwable());

            return;
        }

        try {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client == null) {
                Util.notifyError("Client is null", new Throwable());

                return;
            }

            client.execute(ScreenManager::customCloseHandledScreen);
        } catch (Exception exception) {
            Util.notifyError("Error closing GUI", exception);
        }
    }

    private static void customCloseHandledScreen() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.player == null) {
                Util.notifyError("Player is null, cannot close screen", new Throwable());

                return;
            }

            client.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(client.player.currentScreenHandler.syncId));
            client.setScreen(null);
            client.player.currentScreenHandler = client.player.playerScreenHandler;
        } catch (Exception exception) {
            Util.notifyError("Error encountered while closing screen with custom method", exception);

            throw new RuntimeException(exception);
        }
    }
}
