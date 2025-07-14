package com.github.mkram17.bazaarutils.events;

import com.github.mkram17.bazaarutils.misc.orderinfo.OrderData;
import lombok.Getter;

//TODO use this instead of OutdatedOrderEvent
public class BazaarChatEvent {
    public enum BazaarEventTypes {
        ORDER_CREATED,
        ORDER_CANCELLED,
        ORDER_FILLED,
        ORDER_CLAIMED,
        INSTA_SELL,
        INSTA_BUY
    }

    @Getter
    private final BazaarEventTypes type;
    @Getter
    private final OrderData order;

    public BazaarChatEvent(BazaarEventTypes type, OrderData order) {
        this.type = type;
        this.order = order;
    }
}
