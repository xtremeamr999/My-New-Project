package com.github.mkram17.bazaarutils.events;

import com.github.mkram17.bazaarutils.misc.orderinfo.OrderData;
import lombok.Getter;
import lombok.Setter;

//TODO use this instead of OutdatedOrderEvent
public class BazaarOrderEvent {
    public enum BazaarEventTypes {
        ORDER_CREATED,
        ORDER_CANCELLED,
        ORDER_FILLED,
        ORDER_CLAIMED
    }

    @Getter
    private final BazaarEventTypes type;
    @Getter
    private final OrderData order;

    public BazaarOrderEvent(BazaarEventTypes type, OrderData order) {
        this.type = type;
        this.order = order;
    }
}
