package com.github.mkram17.bazaarutils.Events;

import com.github.mkram17.bazaarutils.Utils.ItemData;
import com.github.mkram17.bazaarutils.Utils.Util;
import com.github.mkram17.bazaarutils.config.BUConfig;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

//TODO seems like there are still some places where it doesnt round. ex 10.8723333 not 10.9
public class ChatHandler {
    private enum messageTypes {BUYORDER, SELLORDER, FILLED, CLAIMED, CANCELLED}

    public static void subscribe() {
        registerBazaarChat();
        registerStashRemover();
    }

    //TODO there is a small blank message when stash messages are removed, try to fix this -- medium priority
    public static void registerStashRemover(){
        //five? separate messages
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if(BUConfig.get().stashMessages.shouldRemoveMessages())
                return !message.getString().contains("materials stashed away") && !message.getString().contains("material stashed") && !message.getString().contains("to pick them up") && !message.getString().equals(" ") && !message.getString().equals("  ");
            return true;
        });
    }

    public static void registerBazaarChat() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!(message.getString().contains("[Bazaar]"))) return;
            if (!message.getSiblings().isEmpty()) {
                if (message.getSiblings().get(1).getString().contains("escrow")
                        || message.getSiblings().get(1).getString().contains("Submitting")
                        || message.getSiblings().get(1).getString().contains("Executing")
                        || message.getSiblings().get(1).getString().contains("Claiming"))
                    return;
            }

            ArrayList<Text> siblings = new ArrayList<>(message.getSiblings());

            String itemName;
            int volume;
            double price;
            ItemData item;
            messageTypes messageType = null;
            List<Text> messageSiblings = message.getSiblings();

            if (siblings.isEmpty() && message.getString().contains("was filled!")) messageType = messageTypes.FILLED;
            if (siblings.size() >= 3 && siblings.get(2).getString().contains("Buy Order Setup!"))
                messageType = messageTypes.BUYORDER;
            if (siblings.size() >= 3 && siblings.get(2).getString().contains("Sell Offer Setup!"))
                messageType = messageTypes.SELLORDER;
            if(siblings.size() >= 3 && siblings.get(2).getString().contains("Claimed")) messageType = messageTypes.CLAIMED;
            if(siblings.size() >= 5 && siblings.get(2).getString().contains("Cancelled")) messageType = messageTypes.CANCELLED;

            if (messageType == messageTypes.BUYORDER || messageType == messageTypes.SELLORDER) {
                itemName = Util.removeFormatting(siblings.get(5).getString());
                volume = Integer.parseInt(siblings.get(3).getString().replace(",", ""));
                String totalPriceString = siblings.get(Util.findComponentIndex(siblings, "for")+1).getString().replace(",", "");
                totalPriceString = siblings.get(Util.findComponentIndex(siblings, "for")+1).getString().replace(",", "").substring(0, totalPriceString.indexOf(" "));
                price = Double.parseDouble(totalPriceString)/volume;
                ItemData itemToAdd;
                if (messageType == messageTypes.SELLORDER) {
                    price /= (1 - BUConfig.get().bzTax);
                    itemToAdd = new ItemData(itemName, price*volume, ItemData.priceTypes.INSTABUY, volume);
                } else
                    itemToAdd = new ItemData(itemName, price*volume, ItemData.priceTypes.INSTASELL, volume);

                //for some reason 52800046 for 4 was on hypixel as 13200011.6 but calculates to 13200011.5. current theory is that buy price wasnt fully accurate, and it rounded up. also was .2 off on sell order for it. obviously problems with big prices
                Util.addWatchedItem(itemToAdd);
                Util.notifyAll(itemName + " was added with a price of " + itemToAdd.getPrice(), Util.notificationTypes.ITEMDATA);
            }

            if (messageType == messageTypes.FILLED) {
                String messageText = Util.removeFormatting(message.getString());
                volume = Integer.parseInt(messageText.substring(messageText.indexOf("for") + 4, messageText.indexOf("x")).replace(",", ""));
                itemName = messageText.substring(messageText.indexOf("x") + 2, messageText.indexOf("was") - 1);
                if(messageText.contains("Sell Offer"))
                    item = ItemData.findItemFromChat(itemName, null, volume, ItemData.priceTypes.INSTABUY);
                else
                    item = ItemData.findItemFromChat(itemName, null, volume, ItemData.priceTypes.INSTASELL);
                if(item == null)
                    Util.notifyAll("Could not find item to fill with info vol: "+ volume + " name: " + itemName, Util.notificationTypes.ERROR);
                else {
                    ItemData.setItemFilled(item);
                    Util.notifyAll(item.getName() + "[" + item.getIndex() + "] was filled", Util.notificationTypes.ITEMDATA);
                }
            }

            if (messageType == messageTypes.CLAIMED) {
                handleClaimed(siblings);
            }if (messageType == messageTypes.CANCELLED) {
                handleCancelled(siblings);
            }
        });
    }

    public static void handleCancelled(ArrayList<Text> siblings) {
        double totalPrice;
        ItemData item;
        try {
            String infoString = siblings.get(4).getString();
            if(infoString.contains("coins")) {
                totalPrice = Double.parseDouble(infoString.substring(0, infoString.indexOf(" coins")).replace(",", ""));
                item = ItemData.findItemTotalPrice(totalPrice);
            } else {
                String name = siblings.get(6).getString().trim();
                item = ItemData.findItemFromChat(name, null, 3, null);
            }
            if (item != null) {
                item.remove();
            } else {
                Util.notifyAll("Error finding cancelled item.", Util.notificationTypes.ERROR);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void handleClaimed(ArrayList<Text> siblings) {
        Integer volumeClaimed = null;
        Double price = null;
        String itemName = null;
        ItemData item;
        messageTypes orderType;

        if (siblings.get(6).getString().contains("worth")) orderType = messageTypes.BUYORDER;
        else orderType = messageTypes.SELLORDER;

        try {
            if (orderType == messageTypes.BUYORDER) {
                volumeClaimed = Integer.parseInt(siblings.get(3).getString().replace(",", ""));
                itemName = siblings.get(5).getString().trim();
                String priceString = siblings.get(7).getString();
                price = Double.parseDouble(priceString.substring(0, priceString.indexOf(" coins")).replace(",", ""))/volumeClaimed;
                if(ItemData.getVariables(ItemData::getVolume).contains(volumeClaimed))
                    item = ItemData.findItemFromChat(itemName, price, volumeClaimed, ItemData.priceTypes.INSTASELL);
                else
                    item = ItemData.findItemFromChat(itemName, price, null, ItemData.priceTypes.INSTASELL);
            } else {
//                Util.notifyAll("claimed message, but not worth");
                //TODO figure out when there is a volume included in message
//                volumeClaimed = Integer.parseInt(siblings.get(5).getString().replace(",", ""));
                itemName = siblings.get(7).getString().trim();
                String priceString = siblings.get(9).getString();
                price = Double.parseDouble(priceString.trim().replace(",", ""));
                item = ItemData.findItemFromChat(itemName, price, null, ItemData.priceTypes.INSTABUY);
            }

//TODO fix finding if price is similar -- when it comes from chat message the price error can be greater than maximum rounding
            if (item == null) {
                Util.notifyAll("Could not find claimed item: " + itemName, Util.notificationTypes.ITEMDATA);
                return;
            }
            if (volumeClaimed != null && item.getVolume() == volumeClaimed) {
                Util.notifyAll(item.getGeneralInfo() + " was removed", Util.notificationTypes.ITEMDATA);
                ItemData.removeItem(item);
            } else if(volumeClaimed != null){
                item.setAmountClaimed(item.getAmountClaimed() + volumeClaimed);
                Util.notifyAll(item.getName() + " has claimed " + item.getAmountClaimed() + " out of " + item.getVolume(), Util.notificationTypes.ITEMDATA);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Util.notifyAll("Error in order claim text: " + siblings, Util.notificationTypes.ERROR);
        }
    }

}
