package com.github.mkram17.bazaarutils.features.restrictsell.controls;

public sealed interface SellRestrictionControl<T extends Enum<T>> extends SellRestrictor permits DoubleSellRestrictionControl, StringSellRestrictionControl {
    boolean isEnabled();

    T getRule();
}
