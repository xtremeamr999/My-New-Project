package com.github.mkram17.bazaarutils.Utils;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.Events.ChestLoadedEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ItemUpdater {
    private static ArrayList<ItemStack> orderStacks = new ArrayList<>();
    private static List<ItemStack> orderScreen;
    @EventHandler
    public static void onGUI(ChestLoadedEvent e){
        //TODO make in orders screen method
        if(!BazaarUtils.gui.inBuyOrders())
            return;

        orderScreen = e.getItemStacks();
        orderStacks = findOrders(orderScreen);
        addToWatchedItems(orderStacks);
    }

    private static void addToWatchedItems(ArrayList<ItemStack> orderStacks){
        for(ItemStack stack : orderStacks){
            String customName = stack.getCustomName().getString();
            String name = "";
            boolean isSellOrder;
            double fullPrice;
            int volume;
            List<Text> changedComponents = stack.getComponentChanges().get(DataComponentTypes.LORE).get().styledLines();
            if(customName.contains("BUY")){
                name = customName.substring(4);
                isSellOrder = false;
            } else {
                name = customName.substring(5);
                isSellOrder = true;
            }

            volume = Integer.valueOf(changedComponents.get(13).getString().substring(0, changedComponents.get(13).getString().indexOf(" ")));
//            Util.addWatchedItem();
        }
    }

    private static ArrayList<ItemStack> findOrders(List<ItemStack> orderStacks){
            ArrayList<ItemStack> items = new ArrayList<>();
            int lastBlackPaneIndex = -1, firstAfterIndex = -1;

            for (int i = 0; i < orderStacks.size(); i++) {
                if (orderStacks.get(i).isOf(Items.BLACK_STAINED_GLASS_PANE)) {
                    lastBlackPaneIndex = i;
                } else{
                    break;
                }
            }

            if (lastBlackPaneIndex == orderStacks.size()) return items;

            for (int i = lastBlackPaneIndex + 1; i < orderStacks.size(); i++) {
                if (orderStacks.get(i).isOf(Items.BLACK_STAINED_GLASS_PANE)) {
                    firstAfterIndex = i;
                    break;
                }
            }

            if (firstAfterIndex == -1)
                return items;

            for (int i = lastBlackPaneIndex + 1; i < firstAfterIndex; i++) {
                items.add(orderStacks.get(i));
            }

            return items;
    }
}
