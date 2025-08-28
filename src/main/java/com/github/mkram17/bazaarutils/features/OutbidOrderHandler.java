package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.config.BUConfigGui;
import com.github.mkram17.bazaarutils.events.OutbidOrderEvent;
import com.github.mkram17.bazaarutils.events.handlers.BUListener;
import com.github.mkram17.bazaarutils.misc.orderinfo.BazaarOrder;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderInfoContainer;
import com.github.mkram17.bazaarutils.utils.*;
import com.github.mkram17.bazaarutils.config.BUConfig;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.mkram17.bazaarutils.BazaarUtils.EVENT_BUS;

//TODO change the message number instead of sending more
public class OutbidOrderHandler implements BUListener {

    public static final int OUTBID_ORDER_NOTIFICATIONS = 3; // number of notifications to send when an order becomes outdated

    @Getter @Setter
    private boolean autoOpenEnabled;
    @Getter @Setter
    private boolean notifyOutbid;
    @Getter @Setter
    private boolean notificationSound;

    public OutbidOrderHandler(boolean autoOpenEnabled, boolean notifyOutbid) {
        this.autoOpenEnabled = autoOpenEnabled;
        this.notifyOutbid = notifyOutbid;
        this.notificationSound = true;
    }

    public void subscribe() {
        EVENT_BUS.subscribe(this);
    }

    @EventHandler
    public void onOutbid(OutbidOrderEvent e){
        OrderInfoContainer order = e.getOrder();
        if(!notifyOutbid || !BUConfig.get().userOrders.contains(order)) return;
        if(!(order instanceof BazaarOrder bazaarOrder) || bazaarOrder.getFillStatus() == OrderInfoContainer.Statuses.FILLED) return;

        Text amount = Text.literal(bazaarOrder.getVolume() + "x ").formatted(Formatting.BOLD).formatted(Formatting.DARK_PURPLE);
        Text itemName = Text.literal(bazaarOrder.getName().formatted(Formatting.BOLD).formatted(Formatting.GOLD));

        if (e.isOutbid()) {
            MutableText message = Text.literal("Your " + bazaarOrder.getPriceType().getString().toLowerCase() + " order for ").formatted(Formatting.WHITE)
                    .append(amount)
                    .append(itemName)
                    .append(Text.literal(" is now outdated.").formatted(Formatting.WHITE))
                    .append(Text.literal(" Click to open bazaar orders").formatted(Formatting.GOLD));
            if (BUConfig.get().developerMode) {
                message.append(Text.literal(". Market Price: " + bazaarOrder.getMarketPrice(bazaarOrder.getPriceType()) + " Order Price: " + bazaarOrder.getPricePerItem()));
            }
            Util.tickExecuteLater(2, () -> PlayerActionUtil.notifyChatCommand(message, "managebazaarorders"));
            MinecraftClient client = MinecraftClient.getInstance();
            var player = client.player;
            if (notificationSound && player != null) {
                SoundUtil.notifyMultipleTimes(OUTBID_ORDER_NOTIFICATIONS);
            }
        } else if(e.getOrder().getOutbidStatus() == OrderInfoContainer.Statuses.COMPETITIVE) {
            MutableText message = Text.literal("Your " + bazaarOrder.getPriceType().getString().toLowerCase() + " order for ").formatted(Formatting.WHITE)
                    .append(amount)
                    .append(itemName)
                    .append(Text.literal(" is no longer outdated.").formatted(Formatting.DARK_PURPLE));
            Util.tickExecuteLater(2, () -> PlayerActionUtil.notifyAll(message));
        } else {
            MutableText message = Text.literal("Your " + bazaarOrder.getPriceType().getString().toLowerCase() + " order for ").formatted(Formatting.WHITE)
                    .append(amount)
                    .append(itemName)
                    .append(Text.literal(" has been matched.").formatted(Formatting.YELLOW));
            Util.tickExecuteLater(2, () -> PlayerActionUtil.notifyAll(message));
        }

    }

    @EventHandler
    public void openBazaarOnOutdated(OutbidOrderEvent e) {
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

    public static List<BazaarOrder> getOutdatedOrders() {
        return BUConfig.get().userOrders.stream()
                .filter(order -> order.getOutbidStatus() == OrderInfoContainer.Statuses.OUTBID && order.getFillStatus() != OrderInfoContainer.Statuses.FILLED)
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
                        this::isNotifyOutbid,
                        this::setNotifyOutbid)
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
