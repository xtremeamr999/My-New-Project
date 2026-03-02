package com.github.mkram17.bazaarutils.features.gui.inventory.restrictsell.controls;

import com.github.mkram17.bazaarutils.features.gui.inventory.restrictsell.StringRestrictBy;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.OrderInfo;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigObject
public final class StringSellRestrictionControl implements SellRestrictionControl<StringRestrictBy> {
    @ConfigEntry(
            id = "enabled",
            translation = "bazaarutils.config.inventory.instant_sell_restrictions.rules.restriction.enabled.value"
    )
    public boolean enabled;

    @ConfigEntry(
            id = "name",
            translation = "bazaarutils.config.inventory.instant_sell_restrictions.rules.restriction.name.value"
    )
    @Comment(
            value = "The items' name which if held will lock",
            translation = "bazaarutils.config.inventory.instant_sell_restrictions.rules.restriction.name.description"
    )
    public String name;

    private StringRestrictBy rule = StringRestrictBy.NAME;

    public StringSellRestrictionControl(boolean enabled, String name) {
        this.enabled = enabled;
        this.name = name;
    }

    @Override
    public boolean shouldRestrict(OrderInfo container) {
        return container.getName().equalsIgnoreCase(name);
    }
}