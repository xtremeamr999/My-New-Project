package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.Events.ChestLoadedEvent;
import com.github.mkram17.bazaarutils.Events.ReplaceItemEvent;
import com.github.mkram17.bazaarutils.Events.SlotClickEvent;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.misc.CustomItemButton;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

//TODO search for item matching name and then use that ItemStack for bookmark -- can also use that for full name if it's cut off
public class Bookmark extends CustomItemButton {
    @Getter @Setter
    public String name;
    protected boolean inCorrectGui = false;

    @EventHandler
    protected void checkGui(ChestLoadedEvent event) {
        inCorrectGui = BazaarUtils.gui.inBuyOrderScreen() || BazaarUtils.gui.inInstaBuy() || BazaarUtils.gui.inAnyItemScreen();
        changeVisuals(BUConfig.get().bookmarks.contains(this));
    }

    public Bookmark(String name, Item replacementItem) {
        this.name = name;
        this.slotNumber = 0;
        this.replacementItem = replacementItem.getDefaultStack();
        changeVisuals(false);
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

    }

    private void switchBookmarked(){
        if(BUConfig.get().bookmarks.contains(this)) {
            changeVisuals(false);
            BUConfig.get().bookmarks.remove(this);
        }else {
            changeVisuals(true);
            BUConfig.get().bookmarks.add(this);
        }
//        BUConfig.HANDLER.save();
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

}
