package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.events.BUListener;
import com.github.mkram17.bazaarutils.events.ChestLoadedEvent;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderPriceInfo;
import com.github.mkram17.bazaarutils.utils.ScreenInfo;
import com.github.mkram17.bazaarutils.utils.Util;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.*;

import static com.github.mkram17.bazaarutils.BazaarUtils.EVENT_BUS;

public class InstaSellHighlight implements BUListener {

    @Getter @Setter
    private boolean enabled;

    public InstaSellHighlight(boolean enabled) {
        this.enabled = enabled;
    }

    @EventHandler
    private void onScreenLoad(ChestLoadedEvent e) {
        if (!enabled || !ScreenInfo.getCurrentScreenInfo().inBazaarMainPage())
            return;
        Optional<ItemStack> instaSellItemStack = getInstaSellItemStack(e.getItemStacks());
        if (instaSellItemStack.isEmpty()) {
            Util.notifyError("Could not find insta-sell item stack in Bazaar GUI. Please report this issue.", new Throwable());
            return;
        }
        Map<String, OrderPriceInfo> instaSellOrderData = getInstaSellOrderData(instaSellItemStack.get());
    }

    private Optional<ItemStack> getInstaSellItemStack(List<ItemStack> itemStacks){
        return itemStacks.stream().filter(itemStack -> itemStack.getName().getString().contains("Sell Inventory Now")).findFirst();
    }

    private Map<String, OrderPriceInfo> getInstaSellOrderData(ItemStack instaSellItemStack){
        Map<String, OrderPriceInfo> orderData = new HashMap<>();
        LoreComponent loreComponents = instaSellItemStack.get(DataComponentTypes.LORE);
        if(loreComponents == null){
            return Collections.emptyMap();
        }
        List<Text> loreLines = loreComponents.lines();
        return findInstaSellOrderData(loreLines);
    }

    private Map<String, OrderPriceInfo> findInstaSellOrderData(List<Text> loreLines){
        Map<String, OrderPriceInfo> orderData = new HashMap<>();
        List<Text> itemLoreLines = getItemLoreLines(loreLines);
        for(Text line : itemLoreLines) {
            int volume = getVolume(line);
            double totalPrice = getTotalPrice(line);
            double pricePerUnit = Math.round(totalPrice / volume * 10)/10.0;
            OrderPriceInfo priceInfo = new OrderPriceInfo(pricePerUnit ,OrderPriceInfo.priceTypes.INSTASELL);
            String name = getName(line);

            orderData.put(name,priceInfo);
        }
        return orderData;
    }

    private static List<Text> getItemLoreLines(List<Text> loreLines) {
        int firstItemIndex = Util.componentIndexOf(loreLines, "coins");
        int totalCoinIndex = Util.componentLastIndexOf(loreLines, "coins");
        int lastItemIndex = Util.componentLastIndexOf(loreLines.subList(firstItemIndex, totalCoinIndex), "coins");
        return  loreLines.subList(firstItemIndex, lastItemIndex+1);
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

    @Override
    public void subscribe() {
        EVENT_BUS.subscribe(this);
    }
}
