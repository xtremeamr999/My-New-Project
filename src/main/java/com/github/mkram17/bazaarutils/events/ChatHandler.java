package com.github.mkram17.bazaarutils.events;

import com.github.mkram17.bazaarutils.config.BUConfigGui;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderData;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderPriceInfo;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.SoundUtil;
import com.github.mkram17.bazaarutils.utils.Util;
import com.github.mkram17.bazaarutils.config.BUConfig;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.mkram17.bazaarutils.BazaarUtils.EVENT_BUS;

public class ChatHandler implements BUListener {
    public static final ChatHandler INSTANCE = new ChatHandler();

    public enum messageTypes {BUYORDER, SELLORDER, FILLED, CLAIMED, INSTASELL}

    public static Option<Boolean> createOrderFilledSoundOption() {
        return Option.<Boolean>createBuilder()
                .name(Text.literal("Sound on Order Filled"))
                .description(OptionDescription.of(Text.literal("Plays two short notification sounds when your order is filled.")))
                .binding(true,
                        BUConfig.get()::isOrderFilledSound,
                        BUConfig.get()::setOrderFilledSound)
                .controller(BUConfigGui::createBooleanController)
                .build();
    }

    @Override
    public void subscribe() {
        registerBazaarChat();
        EVENT_BUS.subscribe(this);
    }

    @EventHandler
    private void onOrderCreated(BazaarOrderEvent event) {
        if(!(event.getType() == BazaarOrderEvent.BazaarEventTypes.ORDER_CREATED))
            return;
        OrderData order = event.getOrder();
        BUConfig.get().orderLimit.addOrderToLimit(order.getVolume()*order.getPriceInfo().getPrice());
        Util.addWatchedOrder(order);
        //for some reason 52800046 for 4 was on hypixel as 13200011.6 but calculates to 13200011.5. current theory is that buy price wasnt fully accurate, and it rounded up. also was .2 off on sell order for it. obviously problems with big prices
    }
    
    @EventHandler
    private void onOrderFilled(BazaarOrderEvent event) {
        if(!(event.getType() == BazaarOrderEvent.BazaarEventTypes.ORDER_FILLED))
            return;
        OrderData order = event.getOrder();
        boolean foundOrderMatch = order.findOrderInList(BUConfig.get().watchedOrders).isPresent();
        if (foundOrderMatch) {
            order.setFilled();
            PlayerActionUtil.notifyAll(order.getName() + "[" + order.getIndex() + "] was filled", Util.notificationTypes.ORDERDATA);
        } else {
            Util.notifyError("Could not find item to fill with info vol: " + order.getVolume() + " name: " + order.getName(), new Exception("Order Filled Event error"));
        }
    }
    
    @EventHandler
    private void onOrderClaimed(BazaarOrderEvent event) {
        if(!(event.getType() == BazaarOrderEvent.BazaarEventTypes.ORDER_CLAIMED))
            return;
        OrderData order = event.getOrder();
        if (order.getVolume() >= order.getAmountClaimed()) {
            PlayerActionUtil.notifyAll(order.getGeneralInfo() + " was fully claimed and removed from watched orders", Util.notificationTypes.ORDERDATA);
            order.removeFromWatchedItems();
        }
    }

    public static void registerBazaarChat() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            ArrayList<Text> siblings = new ArrayList<>(message.getSiblings());
            if (!(message.getString().contains("[Bazaar]")) || siblings.isEmpty()) return;
            if (siblings.get(1).getString().contains("escrow")
                    || siblings.get(1).getString().contains("Submitting")
                    || siblings.get(1).getString().contains("Executing")
                    || siblings.get(1).getString().contains("Claiming") || (siblings.size() >= 5 && siblings.get(2).getString().contains("Cancelled")))
                return;

            Optional<messageTypes> messageTypeOptional = getMessageTypes(message, siblings);
            if (messageTypeOptional.isEmpty()) {
                Util.notifyError("Unknown message type in bazaar chat: " + message.getString(), null);
                return;
            }
            messageTypes messageType = messageTypeOptional.get();

            if (messageType == messageTypes.BUYORDER || messageType == messageTypes.SELLORDER) {
                handleOrderCreated(siblings, messageType);
            }
            if (messageType == messageTypes.FILLED) {
                handleFilled(message);
            }

            if (messageType == messageTypes.CLAIMED) {
                handleClaimed(siblings);
            }
            if (messageType == messageTypes.INSTASELL) {
                handleInstaSell(siblings);
            }
        });
    }

    private static Optional<messageTypes> getMessageTypes(Text message, ArrayList<Text> siblings) {
        if (siblings.isEmpty() && message.getString().contains("was filled!"))
            return Optional.of(messageTypes.FILLED);

        if(siblings.size() > 3) {
            if (siblings.get(2).getString().contains("Buy Order Setup!"))
                return Optional.of(messageTypes.BUYORDER);
            if (siblings.get(2).getString().contains("Sell Offer Setup!"))
                return Optional.of(messageTypes.SELLORDER);
            if (siblings.get(2).getString().contains("Claimed"))
                return Optional.of(messageTypes.CLAIMED);
            if (siblings.get(1).getString().contains("Sold"))
                return Optional.of(messageTypes.INSTASELL);
        }
        return Optional.empty();
    }

    public static void handleInstaSell(ArrayList<Text> siblings) {
        String totalPriceString = siblings.get(Util.componentIndexOf(siblings, "for") + 1).getString().replace(",", "");
        totalPriceString = siblings.get(Util.componentIndexOf(siblings, "for") + 1).getString().replace(",", "").substring(0, totalPriceString.indexOf(" "));
        BUConfig.get().orderLimit.addOrderToLimit(Double.parseDouble(totalPriceString));
        PlayerActionUtil.notifyAll(totalPriceString, Util.notificationTypes.FEATURE);
    }

    private static void handleFilled(Text message) {
        String messageText = Util.removeFormatting(message.getString());
        String itemName;
        int volume;
        try {
            int forIndex = messageText.indexOf("for");
            int xIndex = messageText.indexOf("x");

            if (forIndex == -1 || xIndex == -1 || forIndex >= xIndex) {
                Util.notifyError("Invalid FILLED message format - missing 'for' or 'x': " + messageText, null);
                return;
            }

            String volumeStr = messageText.substring(forIndex + 4, xIndex).trim().replace(",", "");
            if (volumeStr.isEmpty()) {
                Util.notifyError("Empty volume string in FILLED message: " + messageText, null);
                return;
            }

            try {
                volume = Integer.parseInt(volumeStr);
            } catch (NumberFormatException e) {
                Util.notifyError("Invalid volume format in FILLED message: " + volumeStr, e);
                return;
            }

            int wasIndex = messageText.indexOf("was", xIndex);
            if (wasIndex == -1 || wasIndex <= xIndex + 2) {
                Util.notifyError("Invalid FILLED message format - missing or misplaced 'was': " + messageText, null);
                return;
            }

            itemName = messageText.substring(xIndex + 2, wasIndex - 1).trim();
            if (itemName.isEmpty()) {
                Util.notifyError("Empty item name in FILLED message: " + messageText, null);
                return;
            }

        } catch (Exception e) {
            Util.notifyError("Failed to parse FILLED message: " + messageText, e);
            return;
        }

        if (!BUConfig.get().isOrderFilledSound())
            SoundUtil.notifyMultipleTimes(2);

        OrderPriceInfo.priceTypes priceType = messageText.contains("Sell Offer") ? OrderPriceInfo.priceTypes.INSTABUY : OrderPriceInfo.priceTypes.INSTASELL;
        OrderPriceInfo itemPriceInfo = new OrderPriceInfo(priceType);
        OrderData item = new OrderData(itemName, volume, itemPriceInfo);

        EVENT_BUS.post(new BazaarOrderEvent(BazaarOrderEvent.BazaarEventTypes.ORDER_FILLED, item));
    }

    private static void handleOrderCreated(ArrayList<Text> siblings, messageTypes messageType) {
        String itemName = Util.removeFormatting(getName(siblings));
        int volume = Integer.parseInt(siblings.get(3).getString().replace(",", ""));

        String totalPriceString = siblings.get(Util.componentLastIndexOf(siblings, "for") + 1).getString().replace(",", "");
        totalPriceString = totalPriceString.substring(0, totalPriceString.indexOf(" "));
        double price = Double.parseDouble(totalPriceString) / volume;
        if (messageType == messageTypes.SELLORDER) {
            //the price calculated before is ignoring tax, so must be added to find the actual price (which is used in tooltips etc.)
            price /= ((100 - BUConfig.get().bzTax) / 100);
        }

        OrderPriceInfo priceInfo = new OrderPriceInfo(price, messageType == messageTypes.BUYORDER ? OrderPriceInfo.priceTypes.INSTASELL : OrderPriceInfo.priceTypes.INSTABUY);
        OrderData orderToAdd = new OrderData(itemName, volume, priceInfo);
        EVENT_BUS.post(new BazaarOrderEvent(BazaarOrderEvent.BazaarEventTypes.ORDER_CREATED, orderToAdd));
    }

    private static String getName(List<Text> siblings) {
        if (siblings.size() == 10) {
            return Util.removeFormatting(siblings.get(6).getString());
        } else {
            return Util.removeFormatting(siblings.get(5).getString());
        }
    }

    public static void handleClaimed(ArrayList<Text> siblings) {
        Optional<OrderData> orderOptional;
        try {
            if (siblings.get(6).getString().contains("worth")) {
                orderOptional = getClaimedBuyOrder(siblings);
            } else {
                orderOptional = getClaimedSellOrder(siblings);
            }
        } catch (Exception e) {
            Util.notifyError("Error in order claim text: " + siblings, e);
            return;
        }
        if (orderOptional.isEmpty()) {
            Util.notifyError("Could not find claimed order in watched orders", new Exception("Order Claim Error"));
            return;
        }
        OrderData order = orderOptional.get();
        PlayerActionUtil.notifyAll(order.getName() + " has claimed " + order.getAmountClaimed() + " out of " + order.getVolume(), Util.notificationTypes.ORDERDATA);
        EVENT_BUS.post(new BazaarOrderEvent(BazaarOrderEvent.BazaarEventTypes.ORDER_CLAIMED, order));
    }

    private static Optional<OrderData> getClaimedBuyOrder(ArrayList<Text> siblings){
        // Parse volume with validation
        String volumeStr = siblings.get(3).getString().replace(",", "").trim();
        if (volumeStr.isEmpty()) {
            Util.notifyError("Empty volume string in claimed order", null);
            return Optional.empty();
        }

        int volumeClaimed = Integer.parseInt(volumeStr);

        String itemName = siblings.get(5).getString().trim();
        if (itemName.isEmpty()) {
            Util.notifyError("Empty item name in claimed order", null);
            return Optional.empty();
        }

        String priceString = siblings.get(7).getString();

        int coinsIndex = priceString.indexOf(" coins");
        if (coinsIndex == -1) {
            Util.notifyError("Invalid price format - no 'coins' found in: " + priceString, null);
            return Optional.empty();
        }

        String priceStr = priceString.substring(0, coinsIndex).replace(",", "").trim();
        if (priceStr.isEmpty()) {
            Util.notifyError("Empty price string in claimed order", null);
            return Optional.empty();
        }

        double totalPrice = Double.parseDouble(priceStr);

        if (volumeClaimed == 0) {
            Util.notifyError("Cannot divide by zero volume in claimed order", null);
            return Optional.empty();
        }

        double price = totalPrice / volumeClaimed;

        OrderPriceInfo itemPriceInfo = new OrderPriceInfo(price, OrderPriceInfo.priceTypes.INSTASELL);
        OrderData item;
        if (OrderData.getVariables(OrderData::getVolume).contains(volumeClaimed)) {
            item = new OrderData(itemName, volumeClaimed, itemPriceInfo);
        } else {
            item = new OrderData(itemName, null, itemPriceInfo);
        }

        Optional<OrderData> orderOptional = item.findOrderInList(BUConfig.get().watchedOrders);

        if (orderOptional.isEmpty()) {
            PlayerActionUtil.notifyAll("Could not find claimed item: " + itemName, Util.notificationTypes.ORDERDATA);
            return Optional.empty();
        }
        OrderData order = orderOptional.get();
        order.setAmountClaimed(volumeClaimed);
        return orderOptional;
    }

    private static Optional<OrderData> getClaimedSellOrder(ArrayList<Text> siblings){
        // Util.notifyAll("claimed message, but not worth");
        // Sell order claimed messages sometimes include volume and sometimes don't
        // Example with volume: [Bazaar] Claimed 1x ENCHANTED_COAL for 1,234.5 coins!
        // Example without volume: [Bazaar] Claimed ENCHANTED_COAL for 1,234.5 coins!
        Integer volumeClaimed = null;
        String itemName;
        String priceString;

        // Check if volume is present
        if (siblings.get(4).getString().matches("\\d+x")) { // e.g., "1x"
            String volumeStr = siblings.get(4).getString().replace("x", "").trim();
            volumeClaimed = Integer.parseInt(volumeStr);
            itemName = siblings.get(5).getString().trim();
            priceString = siblings.get(7).getString();
        } else {
            itemName = siblings.get(4).getString().trim();
            priceString = siblings.get(6).getString();
        }

        if (itemName.isEmpty()) {
            Util.notifyError("Empty item name in SELLORDER claimed order", new Exception());
            return Optional.empty();
        }

        String priceStr = priceString.trim().replace(",", "");
        if (priceStr.isEmpty()) {
            Util.notifyError("Empty price string in SELLORDER claimed order", new Exception());
            return Optional.empty();
        }

        double price = Double.parseDouble(priceStr);

        OrderPriceInfo priceInfo = new OrderPriceInfo(price, OrderPriceInfo.priceTypes.INSTABUY);
        OrderData item = new OrderData(itemName, null, priceInfo);

        Optional<OrderData> orderOptional = item.findOrderInList(BUConfig.get().watchedOrders);

        if (orderOptional.isEmpty()) {
            PlayerActionUtil.notifyAll("Could not find claimed item: " + itemName, Util.notificationTypes.ORDERDATA);
            return Optional.empty();
        }
        OrderData order = orderOptional.get();
        if (volumeClaimed != null) {
            order.setAmountClaimed(volumeClaimed);
            PlayerActionUtil.notifyAll(order.getName() + " has claimed " + order.getAmountClaimed() + " out of " + order.getVolume(), Util.notificationTypes.ORDERDATA);
        }
        return orderOptional;
    }

}
