package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.config.BUConfigGui;
import com.github.mkram17.bazaarutils.misc.orderinfo.BazaarOrder;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderInfoContainer;
import com.github.mkram17.bazaarutils.config.BUConfig;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//TODO change the message number instead of sending more
public class OutbidOrderHandler {

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

    public static MutableText getOutbidMessage(BazaarOrder order) {
        return createYourOrderForText(order)
                .append(Text.literal(" is now outdated.").formatted(Formatting.WHITE))
                .append(Text.literal(" Click to open bazaar orders").formatted(Formatting.GOLD));
    }
    public static MutableText getCompetitiveMessage(BazaarOrder order) {
        return createYourOrderForText(order)
                .append(Text.literal(" is no longer outdated.").formatted(Formatting.DARK_PURPLE));
    }
    public static MutableText getMatchedMessage(BazaarOrder order) {
        return createYourOrderForText(order)
                .append(Text.literal(" has been matched.").formatted(Formatting.YELLOW));
    }
    private static MutableText createYourOrderForText(BazaarOrder order){
        return Text.literal("Your " + order.getPriceType().getString().toLowerCase() + " order for ").formatted(Formatting.WHITE)
                .append(Text.literal(order.getVolume().toString() + " ").formatted(Formatting.DARK_PURPLE))
                .append(Text.literal(order.getName()).formatted(Formatting.GOLD));
    }

    public static List<BazaarOrder> getOutbidOrders() {
        return BUConfig.get().userOrders.stream()
                .filter(order -> order.getOutbidStatus() == OrderInfoContainer.Statuses.OUTBID && order.getFillStatus() != OrderInfoContainer.Statuses.FILLED)
                .toList();
    }

    public Collection<Option<Boolean>> createOptions() {
        ArrayList<Option<Boolean>> options = new ArrayList<>();
        options.add(Option.<Boolean>createBuilder()
                .name(Text.literal("Open Bazaar on Outbid Orders"))
                .description(OptionDescription.of(Text.literal("Automatically open the bazaar after a delay when an order becomes outdated.")))
                .binding(false,
                        this::isAutoOpenEnabled,
                        this::setAutoOpenEnabled)
                .controller(BUConfigGui::createBooleanController)
                .build());
        options.add(Option.<Boolean>createBuilder()
                .name(Text.literal("Chat Notification on Outbid Orders"))
                .description(OptionDescription.of(Text.literal("Sends a message in chat when someone has undercut your order.")))
                .binding(true,
                        this::isNotifyOutbid,
                        this::setNotifyOutbid)
                .controller(BUConfigGui::createBooleanController)
                .build());
        options.add(Option.<Boolean>createBuilder()
                .name(Text.literal("Sound on Outbid Order"))
                .description(OptionDescription.of(Text.literal("Plays three short notification sounds when your order becomes outdated.")))
                .binding(true,
                        this::isNotificationSound,
                        this::setNotificationSound)
                .controller(BUConfigGui::createBooleanController)
                .build());
        return options;
    }
}
