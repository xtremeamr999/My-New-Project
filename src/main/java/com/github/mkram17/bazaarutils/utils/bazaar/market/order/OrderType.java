package com.github.mkram17.bazaarutils.utils.bazaar.market.order;

import lombok.Getter;

@Getter
public enum OrderType {
    BUY,
    SELL;

    private OrderType opposite;

    static {
        SELL.opposite = BUY;
        BUY.opposite = SELL;
    }

    public String getString() {
        return switch (this) {
            case SELL -> "Buy";
            case BUY -> "Sell";
        };
    }
}

