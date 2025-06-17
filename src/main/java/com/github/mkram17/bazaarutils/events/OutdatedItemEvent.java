package com.github.mkram17.bazaarutils.events;

import com.github.mkram17.bazaarutils.misc.orderinfo.OrderData;
import lombok.Getter;
import meteordevelopment.orbit.ICancellable;

public class OutdatedItemEvent implements ICancellable {
    @Getter
    private final OrderData item;
    public OutdatedItemEvent(OrderData item) {
        this.item = item;
    }
    @Override
    public void setCancelled(boolean b) {

    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
