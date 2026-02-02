package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.events.ChestLoadedEvent;
import com.github.mkram17.bazaarutils.events.ReplaceItemEvent;
import com.github.mkram17.bazaarutils.events.SlotClickEvent;
import com.github.mkram17.bazaarutils.events.handlers.BUListener;
import com.github.mkram17.bazaarutils.misc.BUCompatibilityHelper;
import com.github.mkram17.bazaarutils.misc.autoregistration.RegisterWidget;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.OrderInfo;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.OrderType;
import com.github.mkram17.bazaarutils.mixin.AccessorHandledScreen;
import com.github.mkram17.bazaarutils.ui.CustomItemButton;
import com.github.mkram17.bazaarutils.ui.widgets.ItemSlotButtonWidget;
import com.github.mkram17.bazaarutils.utils.*;
import com.github.mkram17.bazaarutils.utils.bazaar.market.price.PricingPosition;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

//Object is created in GUIUtils when in an item's bazaar page
public class Bookmark extends CustomItemButton implements BUListener {
    @Getter
    public final String name;

    @Getter @Setter
    public ItemStack bookmarkedItemStack;

    @Getter
    private final OrderInfo orderInfo;

    private static final int SIGN_SLOT_NUMBER = 45;

    private static final Identifier BASE = Identifier.tryParse(BazaarUtils.MODID, "widget/bookmark_widget_base");
    private static final Identifier HOVER = Identifier.tryParse(BazaarUtils.MODID, "widget/bookmark_widget_hover");

    public static final ButtonTextures SLOT_BUTTON_TEXTURES = new ButtonTextures(
            BASE,
            HOVER
    );

    protected void subscribeToEventBusUnsubscriber() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> BazaarUtils.EVENT_BUS.unsubscribe(this));
    }

    public Bookmark(String name) {
        this.name = name;
        this.slotNumber = 0;
        changeVisuals(isItemBookmarked(this.name));
        this.replacementItem.set(BazaarUtils.CUSTOM_SIZE_COMPONENT, "★");
        this.bookmarkedItemStack = findItemStack(name);
        this.orderInfo = new OrderInfo(name, OrderType.SELL, null, null, null, null);

        subscribe();
    }

    @EventHandler
    protected void replaceItemEvent(ReplaceItemEvent event) {
        try {
            //The bookmark can be null if it was a previously added one, not a potential new one
            if (!super.shouldReplaceItem(event) || (bookmarkedItemStack == null && !BUConfig.get().bookmarks.contains(this))) {
                return;
            }

            if (replacementItem == null) {
                changeVisuals(isItemBookmarked(name));
            }

            event.setReplacement(replacementItem);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    private void onBookmarkClick(SlotClickEvent event) {
        if (!super.wasButtonSlotClicked(event)) {
            return;
        }

        SoundUtil.playSound(BUTTON_SOUND, BUTTON_VOLUME);

        reverseBookmarkStatus();
        bookmarkedItemStack = findItemStack(name);

        BUConfig.scheduleConfigSave();
    }

    public void onWidgetLeftClick() {
        SoundUtil.playSound(BUTTON_SOUND, BUTTON_VOLUME);

        boolean userHasSkyblockerBazaarOverlay = BUCompatibilityHelper.isSkyblockerBazaarOverlayEnabled();

        if (userHasSkyblockerBazaarOverlay) {
            BUCompatibilityHelper.setSkyblockerBazaarOverlayValue(false);
        }

        GUIUtils.clickSlot(SIGN_SLOT_NUMBER, 0);
        GUIUtils.runOnNextSignOpen(event -> GUIUtils.setSignText(name, true));

        if (userHasSkyblockerBazaarOverlay) {
            Util.tickExecuteLater(10, () -> BUCompatibilityHelper.setSkyblockerBazaarOverlayValue(true));
        }
    }

    public void onWidgetShiftClick() {
        BUConfig.get().bookmarks.remove(this);
        BUConfig.scheduleConfigSave();
    }

    private void reverseBookmarkStatus() {
        if (isItemBookmarked(name)) {
            changeVisuals(false);
            BUConfig.get().bookmarks.remove(this);
        } else {
            changeVisuals(true);
            BUConfig.get().bookmarks.add(this);
        }

        BUConfig.scheduleConfigSave();
    }

    private void changeVisuals(boolean bookmarked) {
        if (bookmarked) {
            replacementItem = new ItemStack(Items.GREEN_STAINED_GLASS_PANE, 1);

            replacementItem.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Remove " + name + " Bookmark"));
            replacementItem.set(BazaarUtils.CUSTOM_SIZE_COMPONENT, "⃠ ");
        } else {
            replacementItem = new ItemStack(Items.RED_STAINED_GLASS_PANE, 1);

            replacementItem.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Bookmark " + name));
            replacementItem.set(BazaarUtils.CUSTOM_SIZE_COMPONENT, "★");
        }
    }

    public static String findItemName(ChestLoadedEvent e) {
        String nameFromContainer = findItemNameFromContainer();
        if (!OrderInfo.isValidName(nameFromContainer) || nameFromContainer.length() >= 30 ) {
            return findNameFromItemStacks(e.getItemStacks(), nameFromContainer);
        }
        return nameFromContainer;
    }

    private static String findNameFromItemStacks(List<ItemStack> itemStacks, String nameFromContainer) {
        for (ItemStack stack : itemStacks) {
            if (stack == null) {
                continue;
            }

            if (!stack.isEmpty() && stack.getName().getString().startsWith(nameFromContainer)) {
                return stack.getCustomName().getString();
            }
        }

        return "???";
    }

    private static String findItemNameFromContainer() {
        ScreenInfo screenInfo = ScreenInfo.getCurrentScreenInfo();
        String containerName = screenInfo.getScreenName();

        if (screenInfo.inMenu(ScreenInfo.BazaarMenuType.INSTA_BUY)) {
            return containerName.substring(0, containerName.indexOf("➜")-1);
        } else {
            return containerName.substring(containerName.indexOf("➜") + 2);
        }
    }


    private static ItemStack findItemStack(String name) {
        ScreenHandler handler = GUIUtils.getHandledScreen();

        if (handler == null) {
            return null;
        }

        for (Slot slot : handler.slots) {
            ItemStack itemStack = slot.getStack();
            if (itemStack == null) {
                continue;
            }

            if (!itemStack.isEmpty() && itemStack.getName().getString().startsWith(name)) {
                return itemStack;
            }
        }

        for (Slot slot : handler.slots) {
            ItemStack itemStack = slot.getStack();

            if (!itemStack.isEmpty() && itemStack.getName().getString().contains(name)) {
                return itemStack;
            }
        }

        return Items.DIAMOND.getDefaultStack();
    }

    public static boolean isItemBookmarked(String itemName) {
        return findMatchingBookmark(itemName).isPresent();
    }

    public static Optional<Bookmark> findMatchingBookmark(String itemName) {
        return BUConfig.get().bookmarks.stream().filter(bookmark -> bookmark.getName().equalsIgnoreCase(itemName)).findAny();
    }

    @RegisterWidget
    public static List<ItemSlotButtonWidget> getWidgets() {
        List<ItemSlotButtonWidget> widgets = new ArrayList<>();

        ScreenInfo screenInfo = ScreenInfo.getCurrentScreenInfo();
        boolean isTargetScreen = screenInfo.inMenu(ScreenInfo.BazaarMenuType.BAZAAR_MAIN_PAGE);

        if (!(MinecraftClient.getInstance().currentScreen instanceof AccessorHandledScreen screen) || !isTargetScreen) {
            return Collections.emptyList();
        }

        ItemSlotButtonWidget.ScreenWidgetDimensions dimensions = ItemSlotButtonWidget.getSafeScreenDimensions(screen, screenInfo.getScreenName());

        int buttonSize = 18;
        int spacing = 4;
        int buttonX = dimensions.x() + dimensions.backgroundWidth() + spacing;
        int currentButtonY = dimensions.y() + spacing;

        List<Bookmark> bookmarks = BUConfig.get().bookmarks;

        for (Bookmark bookmark : bookmarks) {
            ItemStack configuredItem = bookmark.getBookmarkedItemStack();

            final ItemStack itemForButton = (configuredItem == null) ? Items.BARRIER.getDefaultStack() : configuredItem;
            MutableText text = Text.literal(bookmark.getName()).formatted(Formatting.BOLD);

            OrderInfo orderInfo = bookmark.getOrderInfo();
            orderInfo.updateMarketPrice();

            Style style = Style.EMPTY.withColor(Formatting.GRAY).withBold(false);
            text.append(Text.literal("\nBuy: " + Util.getPrettyString(orderInfo.getPriceForPosition(PricingPosition.MATCHED, OrderType.SELL)) + " coins").setStyle(style));
            text.append(Text.literal("\nSell: " + Util.getPrettyString(orderInfo.getPriceForPosition(PricingPosition.MATCHED, OrderType.BUY)) + " coins").setStyle(style));

            ItemSlotButtonWidget button = new ItemSlotButtonWidget(
                    buttonX,
                    currentButtonY,
                    buttonSize, buttonSize,
                    SLOT_BUTTON_TEXTURES,
                    (btn) -> {
                        if (MinecraftClient.getInstance().isShiftPressed()) {

                            PlayerActionUtil.notifyAll("Removed " + bookmark.getName() + " bookmark from shift-click. Open Bazaar again to display changes.");
                            bookmark.onWidgetShiftClick();
                        } else {
                            bookmark.onWidgetLeftClick();
                        }

                    },
                    itemForButton,
                    text
            );

            widgets.add(button);
            currentButtonY += buttonSize + spacing;
        }

        return widgets;
    }

    @Override
    public void subscribe() {
        BazaarUtils.EVENT_BUS.subscribe(this);
        subscribeToEventBusUnsubscriber();
    }
}
