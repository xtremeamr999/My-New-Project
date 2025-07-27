package com.github.mkram17.bazaarutils.utils.instasell;

import com.github.mkram17.bazaarutils.misc.orderinfo.OrderPriceInfo;
import com.github.mkram17.bazaarutils.utils.ScreenInfo;
import com.github.mkram17.bazaarutils.utils.Util;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.*;

//NOTE FROM INSTASELLRESTRICTION: if there are items with no buy orders in inv, you get "Some items can't be sold" and there are 2 extra components

public class InstaSellUtil{

    public static List<InstaSellItem> getInstaSellOrders(List<ItemStack> itemStacks) {
        if (!ScreenInfo.getCurrentScreenInfo().inMenu(ScreenInfo.BazaarMenuType.BAZAAR_MAIN_PAGE)) {
            Util.notifyError("Could not get insta-sell orders: not in Bazaar main page.", new Throwable());
            return Collections.emptyList();
        }
        Optional<ItemStack> instaSellItemStack = getInstaSellItemStack(itemStacks);
        if (instaSellItemStack.isEmpty()) {
            Util.notifyError("Could not find insta-sell item stack in Bazaar GUI. Please report this issue.", new Throwable());
            return Collections.emptyList();
        }
        return getInstaSellOrderData(instaSellItemStack.get());
    }

    public static Optional<ItemStack> getInstaSellItemStack(List<ItemStack> itemStacks){
        return itemStacks.stream().filter(itemStack -> itemStack.getName().getString().contains("Sell Inventory Now")).findFirst();
    }

    private static List<InstaSellItem> getInstaSellOrderData(ItemStack instaSellItemStack){
        List<InstaSellItem> orderData = new ArrayList<>();
        LoreComponent loreComponents = instaSellItemStack.get(DataComponentTypes.LORE);
        if(loreComponents == null){
            return Collections.emptyList();
        }
        List<Text> loreLines = loreComponents.lines();
        return findInstaSellOrderData(loreLines);
    }

    private static List<InstaSellItem> findInstaSellOrderData(List<Text> loreLines){
        List<InstaSellItem> orderData = new ArrayList<>();
        List<Text> itemLoreLines = getItemLoreLines(loreLines);
        for(Text line : itemLoreLines) {
            int volume = getVolume(line);
            double totalPrice = getTotalPrice(line);
            double pricePerUnit = Math.round(totalPrice / volume * 10)/10.0;
            OrderPriceInfo priceInfo = new OrderPriceInfo(pricePerUnit ,OrderPriceInfo.priceTypes.INSTASELL);
            String name = getName(line);

            InstaSellItem instaSellItem = new InstaSellItem(name, priceInfo);
            orderData.add(instaSellItem);
        }
        return orderData;
    }

    private static List<Text> getItemLoreLines(List<Text> loreLines) {
        int firstItemIndex = Util.componentIndexOf(loreLines, "coins");
        int totalCoinIndex = Util.componentLastIndexOf(loreLines, "coins");

        if(firstItemIndex == -1 || totalCoinIndex == -1) {
            return Collections.emptyList();
        }

        int lastItemIndex = Util.componentLastIndexOf(loreLines.subList(0, totalCoinIndex-1), "coins");

        return loreLines.subList(firstItemIndex, lastItemIndex+1);
    }

    private static int getVolume(Text text){
        String volumeString = text.getSiblings().get(1).getString();
        return Util.parseNumber(volumeString);
    }
    private static String getName(Text text){
        String nameString = text.getSiblings().get(3).getString();
        return nameString.trim();
    }
    private static double getTotalPrice(Text text){
        String priceString = text.getSiblings().get(5).getString();
        return Util.parseNumber(priceString);
    }
}