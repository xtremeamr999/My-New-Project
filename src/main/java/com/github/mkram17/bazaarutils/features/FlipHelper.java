package com.github.mkram17.bazaarutils.features;


import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.events.*;
import com.github.mkram17.bazaarutils.misc.CustomItemButton;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderData;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderPriceInfo;
import com.github.mkram17.bazaarutils.utils.GUIUtils;
import com.github.mkram17.bazaarutils.utils.SoundUtil;
import com.github.mkram17.bazaarutils.utils.Util;
import dev.isxander.yacl3.api.Option;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.mkram17.bazaarutils.BazaarUtils.EVENT_BUS;

//TODO switch to finding market price without finding the OrderData first. Then, ItemUpdater should handle fixing it. Or just do it that way for redundancy.
public class FlipHelper extends CustomItemButton implements BUListener {

    private static final int FLIP_HELPER_BUTTON_SLOT = 15;
    private static final int FLIP_ORDER_SLOT = 13;
    private static final Pattern PRICE_PATTERN = Pattern.compile("([\\d,.]+) coins");
    private static final Pattern VOLUME_PATTERN = Pattern.compile("([\\d,]+)");
    private static final String FLIP_ORDER_IDENTIFIER = "Flip Order";
    private static final String CANCEL_ORDER_IDENTIFIER = "Cancel Order";
    private static final String CANNOT_CANCEL_IDENTIFIER = "Cannot cancel";
    private static final int LORE_LINE_VOLUME = 1;
    private static final int LORE_LINE_PRICE = 3;


    private boolean shouldAddToSign = false;
    @Getter @Setter
    private boolean enabled;
    @Getter
    private final Item buttonItem;
    private OrderData order;

    public FlipHelper(boolean enabled, int slotNumber, Item buttonItem) {
        this.enabled = enabled;
        this.slotNumber = slotNumber;
        this.buttonItem = buttonItem;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChestLoaded(ChestLoadedEvent e) {
        if (!enabled) {
            return;
        }
        if(!inCorrectScreen()){
            resetState();
            return;
        }

        try {
            ItemStack flipOrderSign = getFlipSign(e.getItemStacks()).orElseThrow();
            Optional<OrderData> orderOptional = matchToWatchedOrder(flipOrderSign.getComponents().get(DataComponentTypes.LORE));
            if (orderOptional.isEmpty()) {
                return;
            }
            order = orderOptional.get();
        } catch (Exception ex) {
            Util.notifyError("Error while trying to find flip item in Flip Helper", ex);
        }
    }

    @EventHandler
    public void onSlotClicked(SlotClickEvent event) {
        if (!enabled || event.slot.getIndex() != slotNumber || !inCorrectScreen()) {
            return;
        }

        SoundUtil.playSound(BUTTON_SOUND, BUTTON_VOLUME);
        GUIUtils.clickSlot(FLIP_HELPER_BUTTON_SLOT,0);
        if (order != null)
            shouldAddToSign = true;
    }

    @EventHandler
    public void onSignOpen(SignOpenEvent e){
        if(!shouldAddToSign) return;
        handleFlip();
        shouldAddToSign = false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void replaceItemEvent(ReplaceItemEvent event) {
        if(!enabled || !(event.getSlotId() == slotNumber) || !inCorrectScreen() || buttonItem == null)
            return;

        ItemStack itemStack = new ItemStack(buttonItem, 1);
        itemStack.set(DataComponentTypes.CUSTOM_NAME, getButtonText());
        itemStack.set(BazaarUtils.CUSTOM_SIZE_COMPONENT, getButtonStackSize());
        event.setReplacement(itemStack);
    }

    private Text getButtonText() {
        double flipPrice = order.getFlipPrice();
        if (flipPrice == 0) {
            return Text.literal("There are no competing sell offers.").formatted(Formatting.DARK_PURPLE);
        } else if (order == null) {
            return Text.literal("Could not find order").formatted(Formatting.DARK_PURPLE);
        } else {
            return Text.literal("Flip order for " + Util.getPrettyNumber(flipPrice) + " coins").formatted(Formatting.DARK_PURPLE);
        }
    }

    private String getButtonStackSize() {
        double flipPrice = order.getFlipPrice();
        if (order.getFlipPrice() == 0) {
            return "ANY";
        } else if (order == null) {
            return "???";
        } else {
            return String.valueOf(Util.getPrettyNumber(flipPrice));
        }
    }

    private void resetState() {
        this.order = null;
        this.shouldAddToSign = false;
    }

    private void handleFlip() {
        double flipPrice = order.getFlipPrice();
        if(order != null && flipPrice != 0 && GUIUtils.wasLastChestFlip()) {
            GUIUtils.setSignText(Double.toString(Util.getPrettyNumber(flipPrice)), true);
            order.flipItem(flipPrice);
        }
    }

    private Optional<ItemStack> getFlipSign(List<ItemStack> chestItemStacks) {
        for (ItemStack itemStack : chestItemStacks) {
            if (itemStack == null || itemStack.isEmpty()) {
                continue;
            }

            if (itemStack.getName().getString().contains(FLIP_ORDER_IDENTIFIER)) {
                LoreComponent lore = itemStack.getComponents().get(DataComponentTypes.LORE);
                if (lore != null) {
                    return Optional.of(itemStack);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<OrderPriceInfo> getOrderPriceInfo(LoreComponent lore) {
        if (lore.lines().size() <= LORE_LINE_PRICE) return Optional.empty();

        String priceLine = lore.lines().get(LORE_LINE_PRICE).getString();
        Matcher matcher = PRICE_PATTERN.matcher(priceLine);

        if (matcher.find()) {
            try {
                double orderPrice = Double.parseDouble(matcher.group(1).replace(",", ""));
                return Optional.of(new OrderPriceInfo(orderPrice, OrderPriceInfo.priceTypes.INSTASELL));
            } catch (NumberFormatException e) {
                Util.notifyError("Error while trying to parse order price in Flip Helper", e);
            }
        }
        return Optional.empty();
    }

    private Optional<Integer> getVolumeFilled(LoreComponent lore) {
        if (lore.lines().size() <= LORE_LINE_VOLUME) return Optional.empty();

        String volumeLine = lore.lines().get(LORE_LINE_VOLUME).getString();
        Matcher matcher = VOLUME_PATTERN.matcher(volumeLine);

        if (matcher.find()) {
            try {
                return Optional.of(Integer.parseInt(matcher.group(1).replace(",", "")));
            } catch (NumberFormatException e) {
                Util.notifyError("Error while trying to parse order volume in Flip Helper", e);
            }
        }
        return Optional.empty();
    }

    private Optional<OrderData> matchToWatchedOrder(LoreComponent lore) {
        Optional<OrderPriceInfo> priceInfoOpt = getOrderPriceInfo(lore);
        Optional<Integer> orderVolumeFilledOpt = getVolumeFilled(lore);

        if (priceInfoOpt.isPresent() && orderVolumeFilledOpt.isPresent()) {
            OrderData tempOrder = new OrderData(null, orderVolumeFilledOpt.get(), priceInfoOpt.get());
            return Optional.ofNullable(tempOrder.findOrderInList(BUConfig.get().watchedOrders));
        }
        return Optional.empty();
    }

    private static boolean inCorrectScreen(){
        return GUIUtils.inFlipGui() && !inCancelOrderScreen();
    }

    private static boolean inCancelOrderScreen() {
        if (!(MinecraftClient.getInstance().currentScreen instanceof GenericContainerScreen inventory)) {
            return false;
        }

        try {
            return cantBeFlippedLineIsPresent(inventory, FLIP_ORDER_SLOT);
        } catch (Exception ex) {
            Util.notifyError("Error while checking if in cancel screen", ex);
            return false;
        }
    }

    private static boolean cantBeFlippedLineIsPresent(GenericContainerScreen inventory, int slot){
        ItemStack itemStack = inventory.getScreenHandler().getInventory().getStack(slot);
        if (itemStack.isEmpty()) {
            return false;
        }

        Text customName = itemStack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName == null || !customName.getString().contains(FLIP_ORDER_IDENTIFIER)) {
            return false;
        }

        LoreComponent lore = itemStack.get(DataComponentTypes.LORE);
        if (lore == null || lore.lines().isEmpty()) {
            return false;
        }
        return Util.findComponentWith(lore.lines(), CANNOT_CANCEL_IDENTIFIER) != null;
    }

    //an item to cancel the order being present means that the order has not been filled or is otherwise not ready to be flipped
//    private static boolean isCancelItem(GenericContainerScreen inventory, int slot) {
//        ItemStack itemStack = inventory.getScreenHandler().getInventory().getStack(slot);
//        if (itemStack.isEmpty()) {
//            return false;
//        }
//
//        Text customName = itemStack.get(DataComponentTypes.CUSTOM_NAME);
//        if (customName != null && customName.getString().contains(CANCEL_ORDER_IDENTIFIER)) {
//            return true;
//        }
//
//        LoreComponent lore = itemStack.get(DataComponentTypes.LORE);
//        if (lore != null) {
//            return lore.lines().stream()
//                    .noneMatch(line -> line.getString().contains(CANNOT_CANCEL_IDENTIFIER));
//        }
//
//        return false;
//    }

    public Option<Boolean> createOption() {
        return super.createOption("Flip Helper",
                "Button in flip order menu to undercut market prices for items.",
                this::isEnabled,
                this::setEnabled);
    }

    @Override
    public void subscribe() {
        EVENT_BUS.subscribe(this);
    }
}

