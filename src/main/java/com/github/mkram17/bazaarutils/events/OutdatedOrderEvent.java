package com.github.mkram17.bazaarutils.events;

import com.github.mkram17.bazaarutils.misc.orderinfo.BazaarOrder;
import lombok.Getter;
import meteordevelopment.orbit.ICancellable;

public class OutdatedOrderEvent implements ICancellable {
    @Getter
    private final BazaarOrder order;
    @Getter
    private boolean isOutdated;
    public OutdatedOrderEvent(BazaarOrder order, boolean isOutdated) {
        this.order = order;
        this.isOutdated = isOutdated;
    }
    @Override
    public void setCancelled(boolean b) {

    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
