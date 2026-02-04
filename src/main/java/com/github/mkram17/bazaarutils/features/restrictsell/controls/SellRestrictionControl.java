package com.github.mkram17.bazaarutils.features.restrictsell.controls;

import com.github.mkram17.bazaarutils.features.restrictsell.InstaSellRestrictions;
import com.github.mkram17.bazaarutils.features.restrictsell.RestrictInstaSellBy;
import lombok.Getter;
import lombok.Setter;

public abstract class SellRestrictionControl implements SellRestrictor{
    @Getter
    @Setter
    private boolean enabled = true;
    @Getter @Setter
    private RestrictInstaSellBy rule;

    SellRestrictionControl(RestrictInstaSellBy rule) {
        this.rule = rule;
    }
}
