package com.github.mkram17.bazaarutils.features.restrictsell.controls;

import com.github.mkram17.bazaarutils.features.restrictsell.InstaSellRestrictions;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.OrderInfo;
import lombok.Getter;
import lombok.Setter;

public class DoubleSellRestrictionControl extends SellRestrictionControl{
    @Getter
    @Setter
    private double amount;
    public DoubleSellRestrictionControl(InstaSellRestrictions.restrictBy rule, double amount) {
        super(rule);
        this.amount = amount;
    }

    @Override
    public boolean shouldRestrict(OrderInfo container) {
        return container.getPricePerItem() > amount;
    }
}