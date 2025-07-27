package com.github.mkram17.bazaarutils.features.restrictsell;

import lombok.Getter;
import lombok.Setter;

public class SellRestrictionControl {
    @Getter
    @Setter
    private boolean enabled = true;
    @Getter @Setter
    private InstaSellRestrictions.restrictBy rule;
    @Getter @Setter
    private double amount;
    @Getter @Setter
    private String name;

    public SellRestrictionControl(InstaSellRestrictions.restrictBy rule, double amount) {
        this.rule = rule;
        this.amount = amount;
    }
    public SellRestrictionControl(InstaSellRestrictions.restrictBy rule, String name) {
        this.rule = rule;
        this.name = name;
    }
}
