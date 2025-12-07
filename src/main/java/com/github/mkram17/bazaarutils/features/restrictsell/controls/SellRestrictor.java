package com.github.mkram17.bazaarutils.features.restrictsell.controls;

import com.github.mkram17.bazaarutils.misc.orderinfo.PriceInfoContainer;

public interface SellRestrictor {
    public boolean shouldRestrict(PriceInfoContainer container);
}
