package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.config.BUConfigGui;
import com.github.mkram17.bazaarutils.events.BUListener;
import com.github.mkram17.bazaarutils.events.OutdatedOrderEvent;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderData;
import com.github.mkram17.bazaarutils.utils.GUIUtils;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.SoundUtil;
import com.github.mkram17.bazaarutils.utils.Util;
import com.github.mkram17.bazaarutils.config.BUConfig;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.mkram17.bazaarutils.BazaarUtils.EVENT_BUS;

//TODO change the message number instead of sending more
public class OutdatedOrderHandler implements BUListener {
    public final List<OrderData> outdatedOrders = new ArrayList<>(Collections.emptyList());

    @Getter @Setter
    private boolean autoOpenEnabled;
    @Getter @Setter
    private boolean notifyOutdated;
    @Getter @Setter
    private boolean notificationSound;


    public OutdatedOrderHandler(boolean autoOpenEnabled, boolean notifyOutdated) {
        this.autoOpenEnabled = autoOpenEnabled;
        this.notifyOutdated = notifyOutdated;
        this.notificationSound = true;
    }

    public static void updateOrdersOutdatedStatuses() {
        List<OrderData> previousOutdatedItems = new ArrayList<>(outdatedOrders);
        outdatedOrders.clear();
        for (OrderData item : BUConfig.get().watchedOrders) {
            if (item.getOutdatedStatus() == OrderData.statuses.OUTDATED) {
                outdatedOrders.add(item);
            }
        }

        if (outdatedOrders.isEmpty()) {
//            Util.notifyAll("No outdated items found.", Util.notificationTypes.ITEMDATA);
            return;
        }

        List<OrderData> availableOldOutdated = new ArrayList<>(previousOutdatedItems);

        for (OrderData currentNewOutdatedItem : outdatedOrders) {
            boolean foundMatchInOldList = false;
            OrderData matchedOldItem = null;

            for (OrderData oldItem : availableOldOutdated) {
                if (currentNewOutdatedItem.getName().equals(oldItem.getName()) &&
                        Math.abs(currentNewOutdatedItem.getPriceInfo().getPrice() - oldItem.getPriceInfo().getPrice()) <= currentNewOutdatedItem.getTolerance() &&
                        currentNewOutdatedItem.getVolume().equals(oldItem.getVolume()) &&
                        currentNewOutdatedItem.getPriceInfo().getPriceType() == oldItem.getPriceInfo().getPriceType()) {
                    foundMatchInOldList = true;
                    matchedOldItem = oldItem;
                    break;
                }
            }

            if (foundMatchInOldList) {
                availableOldOutdated.remove(matchedOldItem);
            } else {
                EVENT_BUS.post(new OutdatedOrderEvent(currentNewOutdatedItem, true));
            }
        }

        for (OrderData order : availableOldOutdated) {
            if (BUConfig.get().watchedOrders.contains(order) && order.getFillStatus() != OrderData.statuses.FILLED) {
                EVENT_BUS.post(new OutdatedOrderEvent(order, false));
            }
        }

    }

    @EventHandler
    public void onOutdated(OutdatedOrderEvent e){
        OrderData order = e.getOrder();
        if(notifyOutdated) {
            if(e.isOutdated()) {
                Text amount = Text.literal(order.getVolume() + "x ").formatted(Formatting.BOLD).formatted(Formatting.DARK_PURPLE);
                Text itemName = Text.literal(order.getName().formatted(Formatting.BOLD).formatted(Formatting.GOLD));
                MutableText message = Text.literal("Your " + order.getPriceInfo().getPriceType().getString().toLowerCase() + " order for ").formatted(Formatting.WHITE)
                        .append(amount)
                        .append(itemName)
                        .append(Text.literal(" is now outdated.").formatted(Formatting.WHITE))
                        .append(Text.literal(" Click to open bazaar orders").formatted(Formatting.GOLD));


                Util.tickExecuteLater(2, () -> {
                    if (BUConfig.get().developerMode) {
                        message.append(Text.literal(". Market Price: " + order.getPriceInfo().getMarketPrice() + " Order Price: " + order.getPriceInfo().getPrice()));
                        PlayerActionUtil.notifyChatCommand(message, "managebazaarorders");
                    } else {
                        PlayerActionUtil.notifyChatCommand(message, "managebazaarorders");
                    }
                });
                if (notificationSound)
                    SoundUtil.notifyMultipleTimes(3);
            } else {
                Text amount = Text.literal(e.getOrder().getVolume() + "x ").formatted(Formatting.BOLD).formatted(Formatting.DARK_PURPLE);
                Text itemName = Text.literal(order.getName().formatted(Formatting.BOLD).formatted(Formatting.GOLD));
                MutableText message = Text.literal("[Bazaar Utils] ").formatted(Formatting.GOLD)
                        .append(Text.literal("Your " + order.getPriceInfo().getPriceType().getString().toLowerCase() + " order for ").formatted(Formatting.WHITE))
                        .append(amount)
                        .append(itemName)
                        .append(Text.literal( " is no longer outdated.").formatted(Formatting.DARK_PURPLE));
                PlayerActionUtil.notifyAll(message);
            }
        }




        if(GUIUtils.inBazaar() || !autoOpenEnabled)
            return;
        CompletableFuture.runAsync(() ->{
            for(int i = 3; i >= 1; i--) {
                try {
                    if(i == 3)
                        PlayerActionUtil.notifyAll("Opening bazaar in 3");
                    else
                        PlayerActionUtil.notifyAll(String.valueOf(i));
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }

            PlayerActionUtil.runCommand("managebazaarorders");
        });
    }
    public Collection<Option<Boolean>> createOptions() {
        ArrayList<Option<Boolean>> options = new ArrayList<>();
        options.add(Option.<Boolean>createBuilder()
                .name(Text.literal("Open Bazaar on Outdated Orders"))
                .description(OptionDescription.of(Text.literal("Automatically open the bazaar after a delay when an order becomes outdated.")))
                .binding(false,
                        this::isAutoOpenEnabled,
                        this::setAutoOpenEnabled)
                .controller(BUConfigGui::createBooleanController)
                .build());
        options.add(Option.<Boolean>createBuilder()
                .name(Text.literal("Notify on Outdated Orders"))
                .description(OptionDescription.of(Text.literal("Sends a message in chat when someone has undercut your order.")))
                .binding(true,
                        this::isNotifyOutdated,
                        this::setNotifyOutdated)
                .controller(BUConfigGui::createBooleanController)
                .build());
        options.add(Option.<Boolean>createBuilder()
                .name(Text.literal("Sound for Outdated Orders"))
                .description(OptionDescription.of(Text.literal("Plays three short notification sounds when your order becomes outdated.")))
                .binding(true,
                        this::isNotificationSound,
                        this::setNotificationSound)
                .controller(BUConfigGui::createBooleanController)
                .build());
        return options;
    }

    @Override
    public void subscribe() {
        EVENT_BUS.subscribe(this);
    }
}
