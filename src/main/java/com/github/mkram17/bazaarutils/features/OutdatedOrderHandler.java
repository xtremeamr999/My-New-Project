package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.config.BUConfigGui;
import com.github.mkram17.bazaarutils.events.BazaarDataUpdateEvent;
import com.github.mkram17.bazaarutils.events.OutdatedOrderEvent;
import com.github.mkram17.bazaarutils.events.UserOrdersChangeEvent;
import com.github.mkram17.bazaarutils.misc.autoregistration.RunOnInit;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderData;
import com.github.mkram17.bazaarutils.utils.*;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.mkram17.bazaarutils.BazaarUtils.EVENT_BUS;

//TODO change the message number instead of sending more
public class OutdatedOrderHandler {

    public static final int OUTDATED_ORDER_NOTIFICATIONS = 3; // number of notifications to send when an order becomes outdated

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

    @RunOnInit
    public static void subscribe() {
        EVENT_BUS.subscribe(OutdatedOrderHandler.class);
    }

    @EventHandler
    private static void onBazaarDataUpdate(BazaarDataUpdateEvent e) {
        if (BUConfig.get().userOrders.isEmpty()) {
            return;
        }

        List<OrderData> outdatedOrders = getOutdatedOrders();
        postOutdatedOrderEvents(outdatedOrders);
    }

    @EventHandler
    private static void onUserOrderChange(UserOrdersChangeEvent e) {
        if(e.getChangeType() == UserOrdersChangeEvent.ChangeTypes.REMOVE)
            return;
        List<OrderData> outdatedOrders = getOutdatedOrders();
        postOutdatedOrderEvents(outdatedOrders);
    }

    @EventHandler
    public void onOutdated(OutdatedOrderEvent e){
        OrderData order = e.getOrder();
        if(!notifyOutdated)
            return;

        Text amount = Text.literal(order.getVolume() + "x ").formatted(Formatting.BOLD).formatted(Formatting.DARK_PURPLE);
        Text itemName = Text.literal(order.getName().formatted(Formatting.BOLD).formatted(Formatting.GOLD));

        if (e.isOutdated()) {
            MutableText message = Text.literal("Your " + order.getPriceInfo().getPriceType().getString().toLowerCase() + " order for ").formatted(Formatting.WHITE)
                    .append(amount)
                    .append(itemName)
                    .append(Text.literal(" is now outdated.").formatted(Formatting.WHITE))
                    .append(Text.literal(" Click to open bazaar orders").formatted(Formatting.GOLD));
            if (BUConfig.get().developerMode) {
                message.append(Text.literal(". Market Price: " + order.getPriceInfo().getMarketPrice() + " Order Price: " + order.getPriceInfo().getPricePerItem()));
            }
            Util.tickExecuteLater(2, () -> {
                    PlayerActionUtil.notifyChatCommand(message, "managebazaarorders");
            });

            if (notificationSound) {
                SoundUtil.notifyMultipleTimes(OUTDATED_ORDER_NOTIFICATIONS);
            }
        } else {
            MutableText message = Text.literal("Your " + order.getPriceInfo().getPriceType().getString().toLowerCase() + " order for ").formatted(Formatting.WHITE)
                    .append(amount)
                    .append(itemName)
                    .append(Text.literal(" is no longer outdated.").formatted(Formatting.DARK_PURPLE));
            Util.tickExecuteLater(2, () -> {
                PlayerActionUtil.notifyAll(message);
            });
        }

    }

    @EventHandler
    public void openBazaarOnOutdated(OutdatedOrderEvent e) {
        ScreenInfo screenInfo = ScreenInfo.getCurrentScreenInfo();
        if(screenInfo.inBazaar() || !autoOpenEnabled)
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

    public static void postOutdatedOrderEvents(List<OrderData> currentOutdatedOrders) {
        List<OrderData> previouslyOutdatedOrders = new ArrayList<>(currentOutdatedOrders);
        // find newly outdated orders (in current list but not in previous)
        currentOutdatedOrders.stream().filter(currentOutdatedOrder -> currentOutdatedOrder.findOrderInList(previouslyOutdatedOrders).isEmpty()).forEach(order -> {
            EVENT_BUS.post(new OutdatedOrderEvent(order, true));
        });
        // find orders that are no longer outdated (in previous list but not in current)
        previouslyOutdatedOrders.stream().filter(previouslyOutdatedOrder -> previouslyOutdatedOrder.findOrderInList(currentOutdatedOrders).isEmpty()).forEach(order -> {
            EVENT_BUS.post(new OutdatedOrderEvent(order, false));
        });
    }

    public static List<OrderData> getOutdatedOrders() {
        return BUConfig.get().userOrders.stream()
                .filter(order -> order.getOutdatedStatus() == OrderData.statuses.OUTDATED && order.getFillStatus() != OrderData.statuses.FILLED)
                .toList();
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
                .name(Text.literal("Chat Notification on Outdated Orders"))
                .description(OptionDescription.of(Text.literal("Sends a message in chat when someone has undercut your order.")))
                .binding(true,
                        this::isNotifyOutdated,
                        this::setNotifyOutdated)
                .controller(BUConfigGui::createBooleanController)
                .build());
        options.add(Option.<Boolean>createBuilder()
                .name(Text.literal("Sound on Outdated Order"))
                .description(OptionDescription.of(Text.literal("Plays three short notification sounds when your order becomes outdated.")))
                .binding(true,
                        this::isNotificationSound,
                        this::setNotificationSound)
                .controller(BUConfigGui::createBooleanController)
                .build());
        return options;
    }
}
