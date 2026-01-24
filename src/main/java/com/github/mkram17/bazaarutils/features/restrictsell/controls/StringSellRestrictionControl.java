package com.github.mkram17.bazaarutils.features.restrictsell.controls;

import com.github.mkram17.bazaarutils.features.restrictsell.InstaSellRestrictions;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.OrderInfo;
import lombok.Getter;
import lombok.Setter;

public class StringSellRestrictionControl extends SellRestrictionControl{
    @Getter
    @Setter
    private String name;
    public StringSellRestrictionControl(String name) {
        super(InstaSellRestrictions.restrictBy.NAME);
        this.name = name;
    }

    @Override
    public boolean shouldRestrict(OrderInfo container) {
        return container.getName().equalsIgnoreCase(name);
    }
}