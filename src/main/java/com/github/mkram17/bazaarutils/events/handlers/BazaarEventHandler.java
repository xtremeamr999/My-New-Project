package com.github.mkram17.bazaarutils.events.handlers;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.events.BUListener;
import com.github.mkram17.bazaarutils.events.BazaarChatEvent;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderData;
import com.github.mkram17.bazaarutils.utils.PlayerActionUtil;
import com.github.mkram17.bazaarutils.utils.Util;
import meteordevelopment.orbit.EventHandler;

import static com.github.mkram17.bazaarutils.BazaarUtils.EVENT_BUS;

public class BazaarEventHandler implements BUListener {
    public static final BazaarEventHandler INSTANCE = new BazaarEventHandler();
    @EventHandler
    private void onOrderCreated(BazaarChatEvent event) {
        if(!(event.getType() == BazaarChatEvent.BazaarEventTypes.ORDER_CREATED))
            return;
        OrderData order = event.getOrder();
        BUConfig.get().orderLimit.addOrderToLimit(order.getVolume()*order.getPriceInfo().getPricePerItem());
        Util.addWatchedOrder(order);
        //for some reason 52800046 for 4 was on hypixel as 13200011.6 but calculates to 13200011.5. current theory is that buy price wasnt fully accurate, and it rounded up. also was .2 off on sell order for it. obviously problems with big prices
    }
    @EventHandler
    private void onSellOrder(BazaarChatEvent event) {
        if(!(event.getType() == BazaarChatEvent.BazaarEventTypes.INSTA_SELL))
            return;
        OrderData order = event.getOrder();
        BUConfig.get().orderLimit.addOrderToLimit(order.getVolume()*order.getPriceInfo().getPricePerItem());
        PlayerActionUtil.notifyAll("Insta sell for " + order, Util.notificationTypes.FEATURE);
        //for some reason 52800046 for 4 was on hypixel as 13200011.6 but calculates to 13200011.5. current theory is that buy price wasnt fully accurate, and it rounded up. also was .2 off on sell order for it. obviously problems with big prices
    }

    @EventHandler
    private void onOrderFilled(BazaarChatEvent event) {
        if(!(event.getType() == BazaarChatEvent.BazaarEventTypes.ORDER_FILLED))
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

    @Override
    public void subscribe() {
        EVENT_BUS.subscribe(this);
    }
}
