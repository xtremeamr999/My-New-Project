package com.github.mkram17.bazaarutils.Utils;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.Events.ChestLoadedEvent;
import com.github.mkram17.bazaarutils.config.BUConfig;
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
        updateWatchedItems(orderStacks);
    }

    private static void updateWatchedItems(ArrayList<ItemStack> orderStacks){
        List<ItemData> foundItems = new ArrayList<>();
        for(ItemStack stack : orderStacks){
            String customName = stack.getCustomName().getString();
            String name = "";
            boolean isSellOrder;
            double unitPrice;
            double fullPrice;
            int volumeFilled;
            int totalVolume;
            List<Text> changedComponents = stack.getComponentChanges().get(DataComponentTypes.LORE).get().styledLines();
            if(customName.contains("BUY")){
                name = customName.substring(4);
                isSellOrder = false;
            } else {
                name = customName.substring(5);
                isSellOrder = true;
            }
            unitPrice = Double.parseDouble(Util.extractTextAfterWord(Util.findComponentWith(changedComponents, "per unit"), "unit:"));
            if(Util.findComponentWith(changedComponents, "Filled") != null) {
                var volumeFilledString = Util.findComponentWith(changedComponents, "Filled");
                volumeFilled = Integer.valueOf(volumeFilledString.substring(8, volumeFilledString.indexOf("/")));
                totalVolume = Integer.valueOf(volumeFilledString.substring(volumeFilledString.indexOf("/") + 1, volumeFilledString.lastIndexOf(" ")));
            } else {
                fullPrice = Util.parseNumber(Util.extractTextAfterWord(Util.findComponentWith(changedComponents, "Worth"), "Worth"));
                totalVolume = (int) Math.round(fullPrice/unitPrice);
            }
            fullPrice = Double.parseDouble(Util.extractTextAfterWord(Util.findComponentWith(changedComponents, "per unit"), "unit:"))*totalVolume;

            ItemData tempItem = isSellOrder ? new ItemData(name, fullPrice, ItemData.priceTypes.INSTABUY, totalVolume) : new ItemData(name, fullPrice, ItemData.priceTypes.INSTASELL, totalVolume);

            //if updateWithItem() returns null addWatchedItem returns, so it is only called when no match is found
            foundItems.add(tempItem);
            Util.addWatchedItem(updateWithItem(tempItem));
        }
        removeOldItems(foundItems);
    }

    private static void removeOldItems(List<ItemData> foundItems){
        for(ItemData item : BUConfig.get().watchedItems){
            if(findItem(item, foundItems) != null)
                continue;
            item.remove();
            Util.notifyAll("Removed " + item.getGeneralInfo() + " from watched items.", Util.notificationTypes.ITEMDATA);
        }
    }
    private static ItemData findItem(ItemData matchingItem, List<ItemData> list) {
        String name = matchingItem.getName();
        double price = matchingItem.getPrice();
        int volume = matchingItem.getVolume();
        ItemData.priceTypes priceType = matchingItem.getPriceType();
                ArrayList<ItemData> itemList = new ArrayList<>();
        for(ItemData item : list){
            if(item.isSimilarPrice(price) &&
                    item.getVolume() == volume + item.getAmountClaimed() &&
                    name.equalsIgnoreCase(item.getName()) &&
                    priceType == item.getPriceType()){
                itemList.add(item);
            }
        }
        if (itemList.isEmpty()) {
            return null;
        }
        if (itemList.size() > 1) {
            itemList.forEach(duplicate -> {
                Util.notifyAll("Duplicate item: " + duplicate.getGeneralInfo(), Util.notificationTypes.ITEMDATA);
            });
        }
        return itemList.getFirst();
    }
    private static ItemData updateWithItem(ItemData foundItem){
        ItemData match = ItemData.findItem(foundItem.getName(), foundItem.getPrice(), foundItem.getVolume(), foundItem.getPriceType());
        if(match == null) {
            Util.notifyAll("No match found", Util.notificationTypes.ITEMDATA);
            return foundItem;
        }
//        Util.notifyAll("Match found", Util.notificationTypes.ITEMDATA);
        if(match.getPrice() != foundItem.getPrice()){
            Util.notifyAll("Updating price of " + match.getName() + " from " + match.getPrice() + " to " + foundItem.getPrice(), Util.notificationTypes.ITEMDATA);
            match.setPrice(foundItem.getPrice());
        }
        return null;
    }

    //TODO  low priority -- there is definitely a better way to do this
    private static ArrayList<ItemStack> findOrders(List<ItemStack> orderScreenStacks){
            ArrayList<ItemStack> items = new ArrayList<>();
            int lastBlackPaneIndex = -1, firstAfterIndex = -1;

            for (int i = 0; i < orderScreenStacks.size(); i++) {
                if (orderScreenStacks.get(i).isOf(Items.BLACK_STAINED_GLASS_PANE)) {
                    lastBlackPaneIndex = i;
                } else{
                    break;
                }
            }

            for (int i = lastBlackPaneIndex + 1; i < orderScreenStacks.size() - 2; i++) {
                if (orderScreenStacks.get(i).isOf(Items.BLACK_STAINED_GLASS_PANE) && orderScreenStacks.get(i+1).isOf(Items.BLACK_STAINED_GLASS_PANE) && orderScreenStacks.get(i+2).isOf(Items.BLACK_STAINED_GLASS_PANE)) {
                    firstAfterIndex = i;
                    break;
                }
            }

            if (firstAfterIndex == -1)
                return items;

            for (int i = lastBlackPaneIndex + 1; i < firstAfterIndex; i++) {
                if(!orderScreenStacks.get(i).isOf(Items.BLACK_STAINED_GLASS_PANE))
                    items.add(orderScreenStacks.get(i));
            }

            return items;
    }
}
