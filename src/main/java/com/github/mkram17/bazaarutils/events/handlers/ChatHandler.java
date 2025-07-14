package com.github.mkram17.bazaarutils.events.handlers;

import com.github.mkram17.bazaarutils.config.BUConfigGui;
import com.github.mkram17.bazaarutils.events.BUListener;
import com.github.mkram17.bazaarutils.events.BazaarChatEvent;
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

    public enum messageTypes {BUYORDER, SELLORDER, FILLED, CLAIMED, INSTASELL, INSTABUY, FLIP_TO_SELLORDER, CANCELLED, UNKNOWN}

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

            messageTypes messageType = messageTypeOptional.orElse(messageTypes.UNKNOWN);

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
            if (messageType == messageTypes.INSTABUY) {
                handleInstaBuy(siblings);
            }
            if (messageType == messageTypes.FLIP_TO_SELLORDER) {
                handleFlip(siblings);
            }
            if (messageType == messageTypes.CANCELLED) {
                handleCancelled(siblings);
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
            if (siblings.get(1).getString().contains("Bought"))
                return Optional.of(messageTypes.INSTABUY);
            if (siblings.get(2).getString().contains("Order Flipped!"))
                return Optional.of(messageTypes.FLIP_TO_SELLORDER);
            if (siblings.get(1).getString().contains("Cancelled"))
                return Optional.of(messageTypes.CANCELLED);
        }
        Util.notifyError("Unknown message type in bazaar chat: " + message.getString(), new Throwable());
        return Optional.empty();
    }
    public static void handleFlip(ArrayList<Text> siblings) {
        String volumeString = siblings.get(3).getString().replace(",", "");
        int volume = Integer.parseInt(volumeString);

        String totalPriceWithCoin = siblings.get(6).getString().replace(",", "");
        String totalPriceString = totalPriceWithCoin.substring(0, totalPriceWithCoin.indexOf(" "));
        double totalPrice = Double.parseDouble(totalPriceString);

        double pricePerUnit = totalPrice / volume;

        String nameWithX = siblings.get(4).getString().trim();
        String name = nameWithX.substring(nameWithX.indexOf("x ")+2);

        OrderPriceInfo priceInfo = new OrderPriceInfo(pricePerUnit, OrderPriceInfo.priceTypes.INSTASELL);
        OrderData order = new OrderData(name, volume, priceInfo);
        EVENT_BUS.post(new BazaarChatEvent(BazaarChatEvent.BazaarEventTypes.INSTA_BUY, order));
    }
    public static void handleCancelled(ArrayList<Text> siblings) {
        String totalPriceString = siblings.get(Util.componentIndexOf(siblings, "for") + 1).getString().replace(",", "");
        totalPriceString = siblings.get(Util.componentIndexOf(siblings, "for") + 1).getString().replace(",", "").substring(0, totalPriceString.indexOf(" "));
        double totalPrice = Double.parseDouble(totalPriceString);

        String volumeString = siblings.get(2).getString().replace(",", "");
        int volume = Integer.parseInt(volumeString);

        double pricePerUnit = totalPrice / volume;

        String name = siblings.get(4).getString().trim();

        OrderPriceInfo priceInfo = new OrderPriceInfo(pricePerUnit, OrderPriceInfo.priceTypes.INSTASELL);
        OrderData order = new OrderData(name, volume, priceInfo);
        EVENT_BUS.post(new BazaarChatEvent(BazaarChatEvent.BazaarEventTypes.INSTA_SELL, order));
    }
    public static void handleInstaSell(ArrayList<Text> siblings) {
        String totalPriceString = siblings.get(Util.componentIndexOf(siblings, "for") + 1).getString().replace(",", "");
        totalPriceString = siblings.get(Util.componentIndexOf(siblings, "for") + 1).getString().replace(",", "").substring(0, totalPriceString.indexOf(" "));
        double totalPrice = Double.parseDouble(totalPriceString);

        String volumeString = siblings.get(2).getString().replace(",", "");
        int volume = Integer.parseInt(volumeString);

        double pricePerUnit = totalPrice / volume;

        String name = siblings.get(4).getString().trim();

        OrderPriceInfo priceInfo = new OrderPriceInfo(pricePerUnit, OrderPriceInfo.priceTypes.INSTASELL);
        OrderData order = new OrderData(name, volume, priceInfo);
        EVENT_BUS.post(new BazaarChatEvent(BazaarChatEvent.BazaarEventTypes.INSTA_SELL, order));
    }
    public static void handleInstaBuy(ArrayList<Text> siblings) {
        String volumeString = siblings.get(2).getString().replace(",", "");
        int volume = Integer.parseInt(volumeString);

        String totalPriceWithCoin = siblings.get(6).getString().replace(",", "");
        String totalPriceString = totalPriceWithCoin.substring(0, totalPriceWithCoin.indexOf(" "));
        double totalPrice = Double.parseDouble(totalPriceString);

        double pricePerUnit = totalPrice / volume;

        String name = siblings.get(4).getString().trim();

        OrderPriceInfo priceInfo = new OrderPriceInfo(pricePerUnit, OrderPriceInfo.priceTypes.INSTASELL);
        OrderData order = new OrderData(name, volume, priceInfo);
        EVENT_BUS.post(new BazaarChatEvent(BazaarChatEvent.BazaarEventTypes.INSTA_BUY, order));
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

        EVENT_BUS.post(new BazaarChatEvent(BazaarChatEvent.BazaarEventTypes.ORDER_FILLED, item));
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
        EVENT_BUS.post(new BazaarChatEvent(BazaarChatEvent.BazaarEventTypes.ORDER_CREATED, orderToAdd));
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
        EVENT_BUS.post(new BazaarChatEvent(BazaarChatEvent.BazaarEventTypes.ORDER_CLAIMED, order));
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
        // Sell order claimed messages sometimes include volume and sometimes don't

        Text volumeComponent = siblings.get(Util.componentIndexOf(siblings, "x")-1);
        String volumeString = volumeComponent.getString();
        int volume = Integer.parseInt(volumeString.replace(",", "").trim());

        Text nameComponent = siblings.get(Util.componentIndexOf(siblings, "x")+1);
        String name = nameComponent.getString().trim();

        Text priceComponent = siblings.get(Util.componentIndexOf(siblings, "at")+1);
        String priceString = priceComponent.getString().replace(",", "").trim();
        double price = Double.parseDouble(priceString);

        OrderPriceInfo priceInfo = new OrderPriceInfo(price, OrderPriceInfo.priceTypes.INSTABUY);
        OrderData item = new OrderData(name, volume, priceInfo);

        Optional<OrderData> orderOptional = item.findOrderInList(BUConfig.get().watchedOrders);

        if (orderOptional.isEmpty()) {
            PlayerActionUtil.notifyAll("Could not find claimed item: " + name, Util.notificationTypes.ORDERDATA);
            return Optional.empty();
        }
        OrderData order = orderOptional.get();
        order.setAmountClaimed(volume);
        PlayerActionUtil.notifyAll(order.getName() + " has claimed " + order.getAmountClaimed() + " out of " + order.getVolume(), Util.notificationTypes.ORDERDATA);

        return orderOptional;
    }

}
