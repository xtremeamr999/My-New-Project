package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.features.util.ConfigurableFeature;
import com.github.mkram17.bazaarutils.features.util.BUToggleableFeature;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.Order;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.OrderInfo;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.OrderStatus;
import com.github.mkram17.bazaarutils.utils.bazaar.market.price.PricingPosition;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//TODO change the message number instead of sending more
public class OutbidOrderHandler implements ConfigurableFeature {
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

    public static MutableText getOutbidMessage(Order order) {
        return createYourOrderForText(order)
                .append(Text.literal(" is now outdated.").formatted(Formatting.WHITE))
                .append(Text.literal(" Click to open bazaar orders").formatted(Formatting.GOLD));
    }
    public static MutableText getCompetitiveMessage(Order order) {
        return createYourOrderForText(order)
                .append(Text.literal(" is no longer outdated.").formatted(Formatting.DARK_PURPLE));
    }
    public static MutableText getMatchedMessage(Order order) {
        return createYourOrderForText(order)
                .append(Text.literal(" has been matched.").formatted(Formatting.YELLOW));
    }
    private static MutableText createYourOrderForText(Order order) {
        return Text.literal("Your " + order.getOrderType().getString().toLowerCase() + " order for ").formatted(Formatting.WHITE)
                .append(Text.literal(order.getVolume().toString() + " ").formatted(Formatting.DARK_PURPLE))
                .append(Text.literal(order.getName()).formatted(Formatting.GOLD));
    }

    public static List<Order> getOutbidOrders() {
        return BUConfig.get().userOrders.stream()
                .filter(order -> order.getPricingPosition() == PricingPosition.OUTBID && order.getStatus() != OrderStatus.FILLED)
                .toList();
    }

    private Collection<Option<Boolean>> createOptions() {
        return new ArrayList<>(List.of(BUToggleableFeature.createOptionHelper("Open Bazaar on Outbid Orders",
                        "Automatically open the bazaar after a delay when an order becomes outdated.",
                        false,
                        this::isAutoOpenEnabled,
                        this::setAutoOpenEnabled),
                BUToggleableFeature.createOptionHelper("Chat Notification on Outbid Orders",
                        "Sends a message in chat when someone has undercut your order.",
                        true,
                        this::isNotifyOutbid,
                        this::setNotifyOutbid),
                BUToggleableFeature.createOptionHelper("Sound on Outbid Order",
                        "Plays three short notification sounds when your order becomes outdated.",
                        true,
                        this::isNotificationSound,
                        this::setNotificationSound))
        );
    }

    @Override
    public void createOption(ConfigCategory.Builder builder) {
        builder.options(this.createOptions());
    }
}
