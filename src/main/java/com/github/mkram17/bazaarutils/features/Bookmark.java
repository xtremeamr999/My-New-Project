package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.Events.ChestLoadedEvent;
import com.github.mkram17.bazaarutils.Events.ReplaceItemEvent;
import com.github.mkram17.bazaarutils.Events.SlotClickEvent;
import com.github.mkram17.bazaarutils.Utils.GUIUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.misc.CustomItemButton;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

//TODO search for item matching name and then use that ItemStack for bookmark -- can also use that for full name if it's cut off
public class Bookmark extends CustomItemButton {
    @Getter @Setter
    public String name;
    @Getter @Setter
    public ItemStack bookmarkedItem;
    protected boolean inCorrectGui = false;
    private final int SIGN_SLOT_NUMBER = 45;

    @EventHandler
    protected void checkGui(ChestLoadedEvent event) {
            BazaarUtils.eventBus.unsubscribe(this);
    }

    public Bookmark(String name, ItemStack bookmarkedItem) {
        this.name = name;
        this.slotNumber = 0;
        this.bookmarkedItem = bookmarkedItem;
        changeVisuals(isBookmarked(this.name));
        this.replacementItem.set(BazaarUtils.CUSTOM_SIZE_COMPONENT, "★");
        inCorrectGui = true;

        BazaarUtils.eventBus.subscribe(this);
    }

    @EventHandler
    protected void replaceItemEvent(ReplaceItemEvent event) {
        if(!inCorrectGui || !super.shouldReplaceItem(event))
            return;

        event.setReplacement(replacementItem);
    }

    //TODO fix every object's events still being active once they are made even when not on screen
    @EventHandler
    private void onClick(SlotClickEvent event){
        if(!inCorrectGui || !super.shouldUseSlot(event))
            return;
        switchBookmarked();
        bookmarkedItem = findItem(name, event);
    }

    public void onLeftClick(){
        GUIUtils.clickSlot(SIGN_SLOT_NUMBER, 0);
        GUIUtils.setSignText(name, true);
    }

    private void switchBookmarked(){
        if(isBookmarked(name)) {
            changeVisuals(false);
            BUConfig.get().bookmarks.remove(this);
        }else {
            changeVisuals(true);
            BUConfig.get().bookmarks.add(this);
        }
        BUConfig.HANDLER.save();
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

    public static String findName(){
        String containerName = BazaarUtils.gui.getContainerName();
        if(BazaarUtils.gui.inInstaBuy())
            return containerName.substring(0, containerName.indexOf("➜")-1);
        if(BazaarUtils.gui.inBuyOrderScreen()){
            containerName = BazaarUtils.gui.getPreviousScreenName();
            return containerName.substring(containerName.indexOf("➜")+2);
        }
        if(BazaarUtils.gui.inAnyItemScreen())
            return containerName.substring(containerName.indexOf("➜")+2);
        return "?";
    }

    private static ItemStack findItem(String name, SlotClickEvent event){
        ScreenHandler handler = event.handledScreen.getScreenHandler();

        for(Slot slot : handler.slots){
            ItemStack itemStack = slot.getStack();

            if (!itemStack.isEmpty() && itemStack.getName().getString().startsWith(name)) {
                return itemStack;
            }
        }
        return null;
    }

    public static boolean isBookmarked(String name){
        return findMatchingBookmark(name) != null;
    }

    public static Bookmark findMatchingBookmark(String name){
        for(Bookmark bookmark : BUConfig.get().bookmarks) {
            if(bookmark.getName().equalsIgnoreCase(name))
                return bookmark;
        }
        return null;
    }

}
