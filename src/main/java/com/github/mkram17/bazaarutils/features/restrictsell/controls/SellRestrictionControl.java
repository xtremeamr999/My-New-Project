package com.github.mkram17.bazaarutils.features.restrictsell.controls;

import com.github.mkram17.bazaarutils.features.restrictsell.InstaSellRestrictions;
import com.github.mkram17.bazaarutils.features.restrictsell.RestrictInstaSellBy;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import lombok.Getter;
import lombok.Setter;

@ConfigObject
public abstract class SellRestrictionControl implements SellRestrictor{
    @Getter @Setter @ConfigEntry(id = "enabled")
    private boolean enabled = true;
    @Getter @Setter @ConfigEntry(id = "rule")
    private RestrictInstaSellBy rule;

    SellRestrictionControl(RestrictInstaSellBy rule) {
        this.rule = rule;
    }
}
