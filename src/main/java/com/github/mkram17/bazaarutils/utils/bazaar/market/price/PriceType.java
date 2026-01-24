package com.github.mkram17.bazaarutils.utils.bazaar.market.price;

import com.github.mkram17.bazaarutils.utils.bazaar.market.order.OrderType;
import lombok.Getter;

@Getter
public enum PriceType {
    INSTABUY {
        @Override
        public OrderType asOrderType() {
            return OrderType.SELL;
        }
    },

    INSTASELL {
        @Override
        public OrderType asOrderType() {
            return OrderType.BUY;
        }
    };

    public abstract OrderType asOrderType();

    public String getString() {
        return switch (this) {
            case INSTASELL -> "Buy";
            case INSTABUY -> "Sell";
        };
    }

    private PriceType opposite;

    static {
        INSTASELL.opposite = INSTABUY;
        INSTABUY.opposite = INSTASELL;
    }
}
