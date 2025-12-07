package com.github.mkram17.bazaarutils.features.restrictsell.controls;

import com.github.mkram17.bazaarutils.features.restrictsell.InstaSellRestrictions;
import com.github.mkram17.bazaarutils.misc.orderinfo.PriceInfoContainer;
import lombok.Getter;
import lombok.Setter;

public class StringSellRestrictionControl extends SellRestrictionControl{
    @Getter
    @Setter
    private String name;
    public StringSellRestrictionControl(InstaSellRestrictions.restrictBy rule, String name) {
        super(rule);
        this.name = name;
    }

    @Override
    public boolean shouldRestrict(PriceInfoContainer container) {
        return false;
    }
}