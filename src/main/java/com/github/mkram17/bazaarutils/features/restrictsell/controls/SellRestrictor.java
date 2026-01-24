package com.github.mkram17.bazaarutils.features.restrictsell.controls;

import com.github.mkram17.bazaarutils.utils.bazaar.market.order.OrderInfo;

public interface SellRestrictor {
    public boolean shouldRestrict(OrderInfo container);
}
