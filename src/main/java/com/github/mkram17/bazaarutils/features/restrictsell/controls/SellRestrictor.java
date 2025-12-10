package com.github.mkram17.bazaarutils.features.restrictsell.controls;

import com.github.mkram17.bazaarutils.misc.orderinfo.OrderInfoContainer;

public interface SellRestrictor {
    public boolean shouldRestrict(OrderInfoContainer container);
}
