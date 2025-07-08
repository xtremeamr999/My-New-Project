package com.github.mkram17.bazaarutils.utils;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.events.BUListener;
import com.github.mkram17.bazaarutils.events.ChestLoadedEvent;
import com.github.mkram17.bazaarutils.features.OutdatedOrderHandler;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderData;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderPriceInfo;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.mkram17.bazaarutils.BazaarUtils.EVENT_BUS;

public class ItemUpdater implements BUListener {
    public static final ItemUpdater INSTANCE = new ItemUpdater();
    private static Inventory lowerChestInventory;

    private static final String BUY_ORDER_PREFIX = "BUY";
    private static final String SELL_ORDER_PREFIX = "SELL";
    private static final String FILLED_LORE = "Filled";
    private static final String PER_UNIT_LORE = "per unit";
    private static final String TO_CLAIM_LORE = "to claim!";
    private static final String ITEMS_LORE = "items";
    private static final String COINS_LORE = " coins";

    @EventHandler(priority = EventPriority.HIGH)
    public static void onGUI(ChestLoadedEvent e) {
        if (!GUIUtils.inOrderScreen()) return;

        lowerChestInventory = e.getLowerChestInventory();
        List<ItemStack> orderScreen = e.getItemStacks();
        List<ItemStack> orderStacks = findOrders(orderScreen);
        updateWatchedItems(orderStacks);
    }

    private static void updateWatchedItems(List<ItemStack> orderStacks) {
        List<OrderData> foundItems = orderStacks.stream()
                .map(ItemUpdater::parseOrderFromItemStack)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        for (OrderData item : foundItems) {
            if (!updateExistingOrderItem(item)) {
                Util.addWatchedOrder(item);
            }
        }

        removeOldItems(foundItems);
        OutdatedOrderHandler.updateOrdersOutdatedStatuses();
    }

    private static Optional<OrderData> parseOrderFromItemStack(ItemStack stack) {
        String customName = stack.getName().getString();
        Optional<? extends LoreComponent> loreComponent = stack.getComponentChanges().get(DataComponentTypes.LORE);
        if (loreComponent == null || loreComponent.isEmpty())
            return Optional.empty();

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
            return Optional.empty();
        }

        double unitPrice = Double.parseDouble(Util.extractTextAfterWord(Util.findComponentWith(lore, PER_UNIT_LORE), "unit:"));
        int totalVolume;
        int volumeFilled = -1;
        int amountClaimed = -1;

        String volumeFilledString = Util.findComponentWith(lore, FILLED_LORE);
        if (volumeFilledString != null) {
            volumeFilled = Util.parseNumber(volumeFilledString.substring(8, volumeFilledString.indexOf("/")));
            totalVolume = Util.parseNumber(volumeFilledString.substring(volumeFilledString.indexOf("/") + 1, volumeFilledString.lastIndexOf(" ")));
        } else {
            totalVolume = Util.parseNumber(lore.get(2).getSiblings().get(1).getString());
        }

        if (volumeFilled != -1) {
            String amountUnclaimedString = Util.findComponentWith(lore, TO_CLAIM_LORE);
            if (amountUnclaimedString != null) {
                int amountUnclaimed;
                if (!amountUnclaimedString.contains(ITEMS_LORE)) {
                    amountUnclaimed = Util.parseNumber(amountUnclaimedString.substring(9, amountUnclaimedString.indexOf(COINS_LORE)));
                } else {
                    amountUnclaimed = Util.parseNumber(amountUnclaimedString.substring(9, amountUnclaimedString.indexOf(ITEMS_LORE) - 1));
                }
                amountClaimed = volumeFilled - amountUnclaimed;
            } else {
                amountClaimed = volumeFilled;
            }
        }

        OrderPriceInfo.priceTypes type = isSellOrder ? OrderPriceInfo.priceTypes.INSTABUY : OrderPriceInfo.priceTypes.INSTASELL;
        OrderPriceInfo priceInfo = new OrderPriceInfo(unitPrice, type);
        OrderData orderData = new OrderData(name, totalVolume, priceInfo);
        orderData.getItemInfo().setItemStack(stack);
        orderData.getItemInfo().setSlotIndex(mapScreenIndexToInventoryIndex(orderData));
        orderData.setTolerance(0.0);

        if (volumeFilled > -1) {
            orderData.setAmountFilled(volumeFilled);
            if (volumeFilled == totalVolume) orderData.setFilled();
        }
        if (amountClaimed > -1) {
            orderData.setAmountClaimed(amountClaimed);
        }

        return Optional.of(orderData);
    }

    //TODO switch to using ItemStack instead of OrderData so it's faster
    private static int mapScreenIndexToInventoryIndex(OrderData item) {
        if (lowerChestInventory == null)
            return -1;

        for (int i = 0; i < lowerChestInventory.size(); i++) {
            ItemStack inventoryStack = lowerChestInventory.getStack(i);
            if (!inventoryStack.isEmpty()) {
                if (inventoryStack.equals(item.getItemInfo().getItemStack())) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static void removeOldItems(List<OrderData> foundItems) {
        List<OrderData> itemsToRemove = BUConfig.get().watchedOrders.stream()
                .filter(watchedItem -> watchedItem.findItemInList(foundItems) == null)
                .toList();

        itemsToRemove.forEach(item -> {
            item.removeFromWatchedItems();
            PlayerActionUtil.notifyAll("Removed " + item.getGeneralInfo() + " from watched items.", Util.notificationTypes.ITEMDATA);
        });

        if (!itemsToRemove.isEmpty()) {
            BUConfig.HANDLER.save();
        }
    }

    private static boolean updateExistingOrderItem(OrderData foundItem) {
        OrderData match = foundItem.findItemInList(BUConfig.get().watchedOrders);
        if (match == null) {
            PlayerActionUtil.notifyAll("No match found for " + foundItem.getName(), Util.notificationTypes.ITEMDATA);
            return false;
        }

        match.getItemInfo().setSlotIndex(foundItem.getItemInfo().getSlotIndex());
        match.getItemInfo().setItemStack(foundItem.getItemInfo().getItemStack());

        boolean updated = false;
        if (match.getTolerance() != 0.0) {
            PlayerActionUtil.notifyAll("Updating maximum rounding of " + match.getName() + " from " + match.getTolerance() + " to 0.0 . Price: " + foundItem.getPriceInfo().getPrice(), Util.notificationTypes.ITEMDATA);
            match.setTolerance(0.0);
            updated = true;
        }
        if (!match.getPriceInfo().getPrice().equals(foundItem.getPriceInfo().getPrice())) {
            PlayerActionUtil.notifyAll("Updating price of " + match.getName() + " from " + match.getPriceInfo().getPrice() + " to " + foundItem.getPriceInfo().getPrice(), Util.notificationTypes.ITEMDATA);
            match.getPriceInfo().setPrice(foundItem.getPriceInfo().getPrice());
            updated = true;
        }
        if (match.getFillStatus() != foundItem.getFillStatus()) {
            PlayerActionUtil.notifyAll("Updating status of " + match.getName() + " from " + match.getFillStatus() + " to " + foundItem.getFillStatus(), Util.notificationTypes.ITEMDATA);
            match.setFillStatus(foundItem.getFillStatus());
            updated = true;
        }
        if (match.getAmountFilled() != foundItem.getAmountFilled()) {
            PlayerActionUtil.notifyAll("Updating volume filled of " + match.getName() + " from " + match.getAmountFilled() + " to " + foundItem.getAmountFilled(), Util.notificationTypes.ITEMDATA);
            match.setAmountFilled(foundItem.getAmountFilled());
            updated = true;
        }
        if (match.getAmountClaimed() != foundItem.getAmountClaimed() && foundItem.getAmountClaimed() >= 0) {
            PlayerActionUtil.notifyAll("Updating amount claimed of " + match.getName() + " from " + match.getAmountClaimed() + " to " + foundItem.getAmountClaimed(), Util.notificationTypes.ITEMDATA);
            match.setAmountClaimed(foundItem.getAmountClaimed());
            updated = true;
        }

        if (updated) {
            BUConfig.HANDLER.save();
        }
        return true;
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

    @Override
    public void subscribe() {
        EVENT_BUS.subscribe(this);
    }
}