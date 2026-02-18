package com.github.mkram17.bazaarutils.features.gui.inventory.restrictsell.controls;

import com.github.mkram17.bazaarutils.utils.bazaar.market.order.OrderInfo;

public interface SellRestrictor {
    boolean shouldRestrict(OrderInfo container);
}
