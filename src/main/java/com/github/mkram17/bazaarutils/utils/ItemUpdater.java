package com.github.mkram17.bazaarutils.utils;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.events.ChestLoadedEvent;
import com.github.mkram17.bazaarutils.misc.autoregistration.RunOnInit;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderData;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderItemInfo;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderPriceInfo;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.*;

import static com.github.mkram17.bazaarutils.BazaarUtils.EVENT_BUS;

public class ItemUpdater {
    private static Inventory lowerChestInventory;

    private static final String BUY_ORDER_PREFIX = "BUY";
    private static final String SELL_ORDER_PREFIX = "SELL";
    private static final String FILLED_LORE = "Filled";
    private static final String PER_UNIT_LORE = "per unit";
    private static final String TO_CLAIM_LORE = "to claim!";
    private static final String ITEMS_LORE = "items";
    private static final String COINS_LORE = " coins";
    private static final String ORDER_VOLUME_LORE = "Order amount: ";
    private static final String OFFER_VOLUME_LORE = "Offer amount: ";

    @EventHandler(priority = EventPriority.HIGH)
    public static void onGUI(ChestLoadedEvent e) {
        ScreenInfo screenInfo = ScreenInfo.getCurrentScreenInfo();
        if (!screenInfo.inMenu(ScreenInfo.BazaarMenuType.ORDER_SCREEN)) return;

        lowerChestInventory = e.getLowerChestInventory();
        List<ItemStack> orderScreen = e.getItemStacks();
        List<ItemStack> orderStacks = findOrders(orderScreen);
        updateWatchedItems(orderStacks);
    }

    private static void updateWatchedItems(List<ItemStack> orderStacks) {
        List<OrderData> foundOrders = orderStacks.stream()
                .map(ItemUpdater::parseOrderFromItemStack)
                .toList();

        BUConfig.get().watchedOrders.clear();
        for (OrderData item : foundOrders) {
            Util.addWatchedOrder(item);
        }
        BUConfig.get().outdatedOrderHandler.postOutdatedOrderEvents();
    }

    private static OrderData parseOrderFromItemStack(ItemStack stack) {
        String customName = stack.getName().getString();
        Optional<? extends LoreComponent> loreComponent = stack.getComponentChanges().get(DataComponentTypes.LORE);
        if (loreComponent == null || loreComponent.isEmpty()) {
            Util.notifyError("Error while parsing order from item stack", new Exception("Lore component is null or empty"));
            return null;
        }

        List<Text> lore = loreComponent.get().styledLines();
        boolean isSellOrder;
        String name;

        if (customName.contains(BUY_ORDER_PREFIX)) {
            name = customName.substring(BUY_ORDER_PREFIX.length() + 1);
            isSellOrder = false;
        } else if (customName.contains(SELL_ORDER_PREFIX)) {
            name = customName.substring(SELL_ORDER_PREFIX.length() + 1);
            isSellOrder = true;
        } else {
            Util.notifyError("Error while parsing order from item stack", new Exception("Could not determine if order is buy or sell"));
            return null;
        }

        Text unitPriceText = Util.findComponentWith(lore, PER_UNIT_LORE);
        if (unitPriceText == null) {
            Util.notifyError("Error while parsing order from item stack", new Exception("Could not find unit price in lore"));
            return null;
        }
        double unitPrice = Double.parseDouble(Util.extractTextAfterWord(unitPriceText.getString(), "unit:"));
        int volume;
        int amountFilled = -1;
        int amountClaimed = -1;

        Text amountFilledText = Util.findComponentWith(lore, FILLED_LORE);
        if (amountFilledText != null) {
            amountFilled = Util.parseNumber(amountFilledText.getString().substring(8, amountFilledText.getString().indexOf("/")));
        }

        Text volumeText = Util.findComponentWith(lore, ORDER_VOLUME_LORE);
        if (volumeText == null) {
            volumeText = Util.findComponentWith(lore, OFFER_VOLUME_LORE);
        }

        if (volumeText != null) {
            var volumeTextSiblings = volumeText.getSiblings();
            volume = Util.parseNumber(volumeTextSiblings.get(1).getString());
        } else {
                Util.notifyError("Error while parsing order from item stack", new Exception("Could not find volume in lore"));
            volume = -1; // should never happen
        }

        if (amountFilled != -1) {
            Text amountUnclaimedText = Util.findComponentWith(lore, TO_CLAIM_LORE);
            if (amountUnclaimedText != null) {
                int amountUnclaimed;
                if (!amountUnclaimedText.getString().contains(ITEMS_LORE)) {
                    amountUnclaimed = Util.parseNumber(amountUnclaimedText.getString().substring(9, amountUnclaimedText.getString().indexOf(COINS_LORE)));
                } else {
                    amountUnclaimed = Util.parseNumber(amountUnclaimedText.getString().substring(9, amountUnclaimedText.getString().indexOf(ITEMS_LORE) - 1));
                }
                amountClaimed = amountFilled - amountUnclaimed;
            } else {
                amountClaimed = amountFilled;
            }
        }

        OrderPriceInfo.priceTypes type = isSellOrder ? OrderPriceInfo.priceTypes.INSTABUY : OrderPriceInfo.priceTypes.INSTASELL;
        OrderPriceInfo priceInfo = new OrderPriceInfo(unitPrice, type);
        OrderItemInfo itemInfo = findItemInfo(stack);
        OrderData orderData = new OrderData(name, volume, priceInfo, itemInfo);

        orderData.setTolerance(0.0);

        if (amountFilled > -1) {
            orderData.setAmountFilled(amountFilled);
            if (Util.genericIsSimilarValue(amountFilled, volume, volume*.05)) {
                orderData.setFilled();
            }
        }
        if (amountClaimed > -1) {
            orderData.setAmountClaimed(amountClaimed);
        }

        return orderData;
    }

    private static OrderItemInfo findItemInfo(ItemStack itemStack) {
        int inventoryIndex = mapScreenIndexToInventoryIndex(itemStack);
        return new OrderItemInfo(inventoryIndex, itemStack);
    }

    //TODO switch to using ItemStack instead of OrderData so it's faster
    private static int mapScreenIndexToInventoryIndex(ItemStack itemStack) {
        if (lowerChestInventory == null)
            return -1;

        for (int i = 0; i < lowerChestInventory.size(); i++) {
            ItemStack inventoryStack = lowerChestInventory.getStack(i);
            if (!inventoryStack.isEmpty()) {
                if (inventoryStack.equals(itemStack)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static ArrayList<ItemStack> findOrders(List<ItemStack> orderScreenStacks) {
        ArrayList<ItemStack> orderStacks = new ArrayList<>();

        for (ItemStack orderScreenStack : orderScreenStacks) {
            if (orderScreenStack.isOf(Items.BLACK_STAINED_GLASS_PANE)) {
                continue;
            }
            if (orderScreenStack.isOf(Items.ARROW)) {
                break; // the back arrow will be the first item that isnt a glass pane, so we know to stop adding items to the orders when we get here
            }
            orderStacks.add(orderScreenStack);
        }

        return orderStacks;
    }

    @RunOnInit
    public static void subscribe() {
        EVENT_BUS.subscribe(ItemUpdater.class);
    }
}