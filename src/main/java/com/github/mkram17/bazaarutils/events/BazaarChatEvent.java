package com.github.mkram17.bazaarutils.events;

import com.github.mkram17.bazaarutils.misc.orderinfo.OrderData;
import lombok.Getter;

//TODO use this instead of OutdatedOrderEvent
public record BazaarChatEvent(@Getter BazaarEventTypes type, @Getter OrderData order) {
    public enum BazaarEventTypes {
        ORDER_CREATED,
        ORDER_CANCELLED,
        ORDER_FILLED,
        ORDER_CLAIMED,
        ORDER_FLIPPED,
        INSTA_SELL,
        INSTA_BUY,
    }
}
