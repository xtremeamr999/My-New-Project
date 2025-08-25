package com.github.mkram17.bazaarutils.events;

import com.github.mkram17.bazaarutils.misc.orderinfo.BazaarOrder;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderInfo;
import lombok.Getter;
import meteordevelopment.orbit.ICancellable;

public class OutbidOrderEvent implements ICancellable {
    @Getter
    private final OrderInfo order;
    @Getter
    private final boolean isOutbid;
    public OutbidOrderEvent(OrderInfo order, boolean isOutbid) {
        this.order = order;
        this.isOutbid = isOutbid;
    }
    @Override
    public void setCancelled(boolean b) {

    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
