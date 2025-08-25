package com.github.mkram17.bazaarutils.events.handlers;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.config.BUConfigGui;
import com.github.mkram17.bazaarutils.events.BazaarChatEvent;
import com.github.mkram17.bazaarutils.misc.autoregistration.RunOnInit;
import com.github.mkram17.bazaarutils.misc.orderinfo.BazaarOrder;
import com.github.mkram17.bazaarutils.misc.orderinfo.PriceInfo;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.Util;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.mkram17.bazaarutils.BazaarUtils.EVENT_BUS;

public class ChatHandler {
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

    private static boolean shouldIgnoreMessage(Text message) {
        String messageString = message.getString();
        return !messageString.contains("[Bazaar]") || message.getSiblings().isEmpty()
                || messageString.contains("Submitting") || messageString.contains("Executing")
                || messageString.contains("Claiming") || messageString.contains("Cancelled")
                || messageString.contains("escrow") || messageString.contains("Cancelling");
    }

    @RunOnInit
    public static void registerBazaarChat() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if(message.getString().contains("Error")) return;

            ArrayList<Text> siblings = new ArrayList<>(message.getSiblings());
            getMessageType(message, siblings).ifPresent(messageType -> {
                switch (messageType) {
                    case ORDER_CREATED -> handleOrderCreated(siblings);
                    case ORDER_FILLED -> handleFilled(message);
                    case ORDER_CLAIMED -> handleClaimed(siblings);
                    case INSTA_SELL -> handleInstaSell(siblings);
                    case INSTA_BUY -> handleInstaBuy(siblings);
                    case ORDER_FLIPPED -> handleFlip(siblings);
                    case ORDER_CANCELLED -> handleCancelled(siblings);
                }
            });
        });
    }

    private static Optional<BazaarChatEvent.BazaarEventTypes> getMessageType(Text message, ArrayList<Text> siblings) {
        if (siblings.isEmpty() && message.getString().contains("was filled!")) {
            return Optional.of(BazaarChatEvent.BazaarEventTypes.ORDER_FILLED);
        }

        if (siblings.size() > 3) {
            String identifier = siblings.get(2).getString();
            if (identifier.contains("Buy Order Setup!") || identifier.contains("Sell Offer Setup!")) {
                return Optional.of(BazaarChatEvent.BazaarEventTypes.ORDER_CREATED);
            }
            if (identifier.contains("Claimed")) return Optional.of(BazaarChatEvent.BazaarEventTypes.ORDER_CLAIMED);
            if (identifier.contains("Order Flipped!"))
                return Optional.of(BazaarChatEvent.BazaarEventTypes.ORDER_FLIPPED);

            identifier = siblings.get(1).getString();
            if (identifier.contains("Sold")) return Optional.of(BazaarChatEvent.BazaarEventTypes.INSTA_SELL);
            if (identifier.contains("Bought")) return Optional.of(BazaarChatEvent.BazaarEventTypes.INSTA_BUY);
            if (identifier.contains("Cancelled")) return Optional.of(BazaarChatEvent.BazaarEventTypes.ORDER_CANCELLED);
        }
        return Optional.empty();
    }

    private static Optional<BazaarOrder> parseOrderData(ArrayList<Text> siblings, int volumeIndex, int nameIndex, int priceIndex) {
        try {
            String volumeString = siblings.get(volumeIndex).getString().replace(",", "");
            int volume = Integer.parseInt(volumeString);

            String name = siblings.get(nameIndex).getString().trim();
            if (name.contains("x ")) { // For flipped orders
                name = name.substring(name.indexOf("x ") + 2);
            }

            String priceString = siblings.get(priceIndex).getString().replace(",", "");
            priceString = priceString.substring(0, priceString.indexOf(" "));
            double totalPrice = Double.parseDouble(priceString);

            double pricePerUnit = totalPrice / volume;

            return Optional.of(new BazaarOrder(name, volume, pricePerUnit, null));
        } catch (Exception e) {
            Util.notifyError("Failed to parse order data from chat: " + siblings, e);
            return Optional.empty();
        }
    }

    private static void processOrderEvent(
            ArrayList<Text> siblings,
            BazaarChatEvent.BazaarEventTypes eventType,
            PriceInfo.priceTypes priceType,
            int volumeIndex,
            int nameIndex,
            int priceIndex
    ) {
        parseOrderData(siblings, volumeIndex, nameIndex, priceIndex).ifPresent(order -> {
            order.setPriceType(priceType);
            EVENT_BUS.post(new BazaarChatEvent(eventType, order));
        });
    }

    public static void handleFlip(ArrayList<Text> siblings) {
        processOrderEvent(siblings, BazaarChatEvent.BazaarEventTypes.ORDER_FLIPPED, PriceInfo.priceTypes.INSTABUY, 3, 4, 6);
    }

    public static void handleCancelled(ArrayList<Text> siblings) {
        int priceIndex = Util.componentIndexOf(siblings, "for") + 1;
        processOrderEvent(siblings, BazaarChatEvent.BazaarEventTypes.ORDER_CANCELLED, PriceInfo.priceTypes.INSTASELL, 2, 4, priceIndex);
    }

    public static void handleInstaSell(ArrayList<Text> siblings) {
        int priceIndex = Util.componentIndexOf(siblings, "for") + 1;
        processOrderEvent(siblings, BazaarChatEvent.BazaarEventTypes.INSTA_SELL, PriceInfo.priceTypes.INSTASELL, 2, 4, priceIndex);
    }

    public static void handleInstaBuy(ArrayList<Text> siblings) {
        processOrderEvent(siblings, BazaarChatEvent.BazaarEventTypes.INSTA_BUY, PriceInfo.priceTypes.INSTABUY, 2, 4, 6);
    }

    private static void handleFilled(Text message) {
        String messageString = Util.removeFormatting(message.getString());
        // Example: "Your Buy Order for 2,304x Mithril was filled!"
        String[] parts = messageString.split(" for |x | was filled!");
        if (parts.length < 3) {
            Util.notifyError("Invalid FILLED message format: " + messageString, new Throwable());
            return;
        }

        try {
            int volume = Integer.parseInt(parts[1].replace(",", ""));
            String itemName = parts[2].trim();

            PriceInfo.priceTypes priceType = messageString.contains("Sell Offer") ? PriceInfo.priceTypes.INSTABUY : PriceInfo.priceTypes.INSTASELL;
            BazaarOrder item = new BazaarOrder(itemName, volume, null, priceType);

            EVENT_BUS.post(new BazaarChatEvent(BazaarChatEvent.BazaarEventTypes.ORDER_FILLED, item));
        } catch (NumberFormatException e) {
            Util.notifyError("Invalid volume format in FILLED message: " + messageString, e);
        } catch (Exception e) {
            Util.notifyError("Failed to parse FILLED message: " + messageString, e);
        }
    }

    private static void handleOrderCreated(ArrayList<Text> siblings) {
        String itemName = Util.removeFormatting(getName(siblings));
        int volume = Integer.parseInt(siblings.get(3).getString().replace(",", ""));

        String totalPriceString = siblings.get(Util.componentLastIndexOf(siblings, "for") + 1).getString().replace(",", "");
        totalPriceString = totalPriceString.substring(0, totalPriceString.indexOf(" "));
        double price = Double.parseDouble(totalPriceString) / volume;

        boolean isSellOrder = siblings.get(2).getString().contains("Sell Offer Setup!");
        if (isSellOrder) {
            //the price calculated before is ignoring tax, so must be added to find the actual price (which is used in tooltips etc.)
            price /= ((100 - BUConfig.get().bzTax) / 100);
        }

        PriceInfo.priceTypes priceType = isSellOrder ? PriceInfo.priceTypes.INSTABUY : PriceInfo.priceTypes.INSTASELL;
        BazaarOrder orderToAdd = new BazaarOrder(itemName, volume, price, priceType);
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
        Optional<BazaarOrder> orderOptional;
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
            Util.notifyError("Could not find claimed order in watched orders", new Throwable("Order Claim Error"));
            return;
        }
        BazaarOrder order = orderOptional.get();
        PlayerActionUtil.notifyAll(order.getName() + " has claimed " + order.getAmountClaimed() + " out of " + order.getVolume(), Util.notificationTypes.ORDERDATA);
        EVENT_BUS.post(new BazaarChatEvent(BazaarChatEvent.BazaarEventTypes.ORDER_CLAIMED, order));
    }

    private static Optional<BazaarOrder> getClaimedBuyOrder(ArrayList<Text> siblings) {
        // Parse volume with validation
        String volumeStr = siblings.get(3).getString().replace(",", "").trim();
        if (volumeStr.isEmpty()) {
            Util.notifyError("Empty volume string in claimed order", new Throwable());
            return Optional.empty();
        }

        int volumeClaimed = Integer.parseInt(volumeStr);

        String itemName = siblings.get(5).getString().trim();
        if (itemName.isEmpty()) {
            Util.notifyError("Empty item name in claimed order", new Throwable());
            return Optional.empty();
        }

        String priceString = siblings.get(7).getString();

        int coinsIndex = priceString.indexOf(" coins");
        if (coinsIndex == -1) {
            Util.notifyError("Invalid price format - no 'coins' found in: " + priceString, new Throwable());
            return Optional.empty();
        }

        String priceStr = priceString.substring(0, coinsIndex).replace(",", "").trim();
        if (priceStr.isEmpty()) {
            Util.notifyError("Empty price string in claimed order", new Throwable());
            return Optional.empty();
        }

        double totalPrice = Double.parseDouble(priceStr);

        if (volumeClaimed == 0) {
            Util.notifyError("Cannot divide by zero volume in claimed order", new Throwable());
            return Optional.empty();
        }

        double price = totalPrice / volumeClaimed;

        BazaarOrder item;
        if (BazaarOrder.getVariables(BazaarOrder::getVolume).contains(volumeClaimed)) {
            item = new BazaarOrder(itemName, volumeClaimed, price, PriceInfo.priceTypes.INSTASELL);
        } else {
            item = new BazaarOrder(itemName, null, price, PriceInfo.priceTypes.INSTASELL);
        }

        return getBazaarOrder(item);
    }

    private static Optional<BazaarOrder> getClaimedSellOrder(ArrayList<Text> siblings) {
        // Sell order claimed messages sometimes include volume and sometimes don't

        Text volumeComponent = siblings.get(Util.componentIndexOf(siblings, "x") - 1);
        String volumeString = volumeComponent.getString();
        int volume = Integer.parseInt(volumeString.replace(",", "").trim());

        Text nameComponent = siblings.get(Util.componentIndexOf(siblings, "x") + 1);
        String name = nameComponent.getString().trim();

        Text priceComponent = siblings.get(Util.componentIndexOf(siblings, "at") + 1);
        String priceString = priceComponent.getString().replace(",", "").trim();
        double price = Double.parseDouble(priceString);

        BazaarOrder item = new BazaarOrder(name, volume, price, PriceInfo.priceTypes.INSTABUY);

        return getBazaarOrder(item);
    }

    private static Optional<BazaarOrder> getBazaarOrder(BazaarOrder item) {
        Optional<BazaarOrder> orderOptional = item.findOrderInList(BUConfig.get().userOrders);

        if (orderOptional.isEmpty()) {
            PlayerActionUtil.notifyAll("Could not find claimed item: " + item.getName(), Util.notificationTypes.ORDERDATA);
            return Optional.empty();
        }
        BazaarOrder order = orderOptional.get();
        order.setAmountClaimed(item.getVolume());
        return orderOptional;
    }
}