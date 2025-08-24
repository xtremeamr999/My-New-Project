package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.events.ChestLoadedEvent;
import com.github.mkram17.bazaarutils.events.ReplaceItemEvent;
import com.github.mkram17.bazaarutils.events.SlotClickEvent;
import com.github.mkram17.bazaarutils.misc.CustomItemButton;
import com.github.mkram17.bazaarutils.misc.autoregistration.RegisterWidget;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderInfo;
import com.github.mkram17.bazaarutils.misc.widgets.ItemSlotButtonWidget;
import com.github.mkram17.bazaarutils.misc.BUCompatibilityHelper;
import com.github.mkram17.bazaarutils.misc.orderinfo.PriceInfo;
import com.github.mkram17.bazaarutils.mixin.AccessorHandledScreen;
import com.github.mkram17.bazaarutils.utils.*;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
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

public class Bookmark extends CustomItemButton {

    @Getter
    public final String name;
    @Getter @Setter
    public ItemStack bookmarkedItemStack;
    @Getter
    private OrderInfo orderInfo;
    private static final int SIGN_SLOT_NUMBER = 45;

    private static final Identifier BASE = Identifier.tryParse(BazaarUtils.MODID, "widget/bookmark_widget_base");
    private static final Identifier HOVER = Identifier.tryParse(BazaarUtils.MODID, "widget/bookmark_widget_hover");
    public static final ButtonTextures SLOT_BUTTON_TEXTURES = new ButtonTextures(
            BASE,
            HOVER);

    @EventHandler
    protected void onGuiLoad(ChestLoadedEvent event) {
            BazaarUtils.EVENT_BUS.unsubscribe(this);
    }

    public Bookmark(String name) {
        this.name = name;
        this.slotNumber = 0;
        changeVisuals(isItemBookmarked(this.name));
        this.replacementItem.set(BazaarUtils.CUSTOM_SIZE_COMPONENT, "★");
        this.bookmarkedItemStack = findItemStack(name);
        this.orderInfo = new OrderInfo(name, 0.0, PriceInfo.priceTypes.INSTABUY);

        BazaarUtils.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    protected void replaceItemEvent(ReplaceItemEvent event) {
        ScreenInfo screenInfo = ScreenInfo.getCurrentScreenInfo();
        try {
            //The bookmark can be null if it was a previously added one, not a potential new one
            if (!super.shouldReplaceItem(event) || (bookmarkedItemStack == null && !BUConfig.get().bookmarks.contains(this)))
                return;

            if (replacementItem == null)
                changeVisuals(isItemBookmarked(name));

            event.setReplacement(replacementItem);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    private void onBookmarkClick(SlotClickEvent event){
        ScreenInfo screenInfo = ScreenInfo.getCurrentScreenInfo();
        if(!super.shouldUseSlot(event))
            return;
        SoundUtil.playSound(BUTTON_SOUND, BUTTON_VOLUME);
        reverseBookmarkStatus();
        bookmarkedItemStack = findItemStack(name);
        Util.scheduleConfigSave();
    }

    public void onWidgetLeftClick(){
        SoundUtil.playSound(BUTTON_SOUND, BUTTON_VOLUME);
        boolean userHasSkyblockerBazaarOverlay = BUCompatibilityHelper.isSkyblockerBazaarOverlayEnabled();

        if(userHasSkyblockerBazaarOverlay) {
            BUCompatibilityHelper.setSkyblockerBazaarOverlayValue(false);
        }

        GUIUtils.clickSlot(SIGN_SLOT_NUMBER, 0);
        GUIUtils.setSignText(name, true);

        if(userHasSkyblockerBazaarOverlay) {
            Util.tickExecuteLater(4, () -> BUCompatibilityHelper.setSkyblockerBazaarOverlayValue(true));
        }
    }

    public void onWidgetShiftClick(){
        BUConfig.get().bookmarks.remove(this);
        Util.scheduleConfigSave();
    }

    private void reverseBookmarkStatus(){
        if(isItemBookmarked(name)) {
            changeVisuals(false);
            BUConfig.get().bookmarks.remove(this);
        }else {
            changeVisuals(true);
            BUConfig.get().bookmarks.add(this);
        }
        Util.scheduleConfigSave();
    }

    private void changeVisuals(boolean bookmarked){
        if(bookmarked) {
            replacementItem = new ItemStack(Items.GREEN_STAINED_GLASS_PANE, 1);
            replacementItem.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Remove " + name + " Bookmark"));
            replacementItem.set(BazaarUtils.CUSTOM_SIZE_COMPONENT, "⃠ ");
        }
        else {
            replacementItem = new ItemStack(Items.RED_STAINED_GLASS_PANE, 1);
            replacementItem.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Bookmark " + name));
            replacementItem.set(BazaarUtils.CUSTOM_SIZE_COMPONENT, "★");
        }
    }

    public static String findItemName(ChestLoadedEvent e){
        String nameFromContainer = findItemNameFromContainer();
        if(!OrderInfo.isValidName(nameFromContainer)) {
            return findNameFromItemStacks(e.getItemStacks(), nameFromContainer);
        }
        return nameFromContainer;
    }

    private static String findNameFromItemStacks(List<ItemStack> itemStacks, String nameFromContainer){
        for(ItemStack stack : itemStacks){
            if(stack == null) continue;
            if (!stack.isEmpty() && stack.getName().getString().startsWith(nameFromContainer)) {
                return stack.getCustomName().getString();
            }
        }
        return "???";
    }

    private static String findItemNameFromContainer(){
        ScreenInfo screenInfo = ScreenInfo.getCurrentScreenInfo();
        String containerName = screenInfo.getContainerName();
        if(screenInfo.inMenu(ScreenInfo.BazaarMenuType.INSTA_BUY)) {
            return containerName.substring(0, containerName.indexOf("➜")-1);
        } else {
            return containerName.substring(containerName.indexOf("➜") + 2);
        }
    }


    private static ItemStack findItemStack(String name){
        ScreenHandler handler = GUIUtils.getHandledScreen();

        assert handler != null;
        for(Slot slot : handler.slots){
            ItemStack itemStack = slot.getStack();
            if(itemStack == null) continue;

            if (!itemStack.isEmpty() && itemStack.getName().getString().startsWith(name)) {
                return itemStack;
            }
        }
        for(Slot slot : handler.slots){
            ItemStack itemStack = slot.getStack();

            if (!itemStack.isEmpty() && itemStack.getName().getString().contains(name)) {
                return itemStack;
            }
        }
        return Items.DIAMOND.getDefaultStack();
    }

    public static boolean isItemBookmarked(String itemName){
        return findMatchingBookmark(itemName).isPresent();
    }

    public static Optional<Bookmark> findMatchingBookmark(String itemName){
        for(Bookmark bookmark : BUConfig.get().bookmarks) {
            if(bookmark.getName().equalsIgnoreCase(itemName))
                return Optional.of(bookmark);
        }
        return Optional.empty();
    }

    @RegisterWidget
    public static List<ItemSlotButtonWidget> getWidgets(){
        List<ItemSlotButtonWidget> widgets = new ArrayList<>();
        ScreenInfo screenInfo = ScreenInfo.getCurrentScreenInfo();
        boolean isTargetScreen = screenInfo.inMenu(ScreenInfo.BazaarMenuType.BAZAAR_MAIN_PAGE);

        if(!(MinecraftClient.getInstance().currentScreen instanceof AccessorHandledScreen screen) || !isTargetScreen)
            return Collections.emptyList();



        ItemSlotButtonWidget.ScreenWidgetDimensions dimensions = ItemSlotButtonWidget.getSafeScreenDimensions(screen, screenInfo.getContainerName());

            int buttonSize = 18;
            int spacing = 4;
            int buttonX = dimensions.x() + dimensions.backgroundWidth() + spacing;
            int currentButtonY = dimensions.y() + spacing;

            List<Bookmark> bookmarks = BUConfig.get().bookmarks;

            for (int i = 0; i < bookmarks.size(); i++) {
                ItemStack configuredItem = bookmarks.get(i).getBookmarkedItemStack();

                final int buttonIndex = i;
                final ItemStack itemForButton = (configuredItem == null) ? Items.BARRIER.getDefaultStack() : configuredItem;
                final Bookmark bookmark = bookmarks.get(buttonIndex);
                MutableText text = Text.literal(bookmark.getName()).formatted(Formatting.BOLD);
                OrderInfo priceInfo = bookmark.getOrderInfo();

                if (priceInfo != null) {
                    Style style = Style.EMPTY.withColor(Formatting.GRAY).withBold(false);
                    text.append(Text.literal("\n"+priceInfo.getMarketPriceString()).setStyle(style));
                    text.append(Text.literal("\n"+priceInfo.getOppositeMarketPriceString()).setStyle(style));
                } else {
                    text.append(Text.literal("\nPrice not available").formatted(Formatting.GRAY));
                }

                ItemSlotButtonWidget button = new ItemSlotButtonWidget(
                        buttonX,
                        currentButtonY,
                        buttonSize, buttonSize,
                        SLOT_BUTTON_TEXTURES,
                        (btn) -> {
                            if (Screen.hasShiftDown()) {
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

}
