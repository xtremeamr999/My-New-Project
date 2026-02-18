package com.github.mkram17.bazaarutils.config.features.gui;

import com.github.mkram17.bazaarutils.features.restrictsell.InstantSellRestrictions;
import com.github.mkram17.bazaarutils.features.restrictsell.NumericRestrictBy;
import com.github.mkram17.bazaarutils.features.restrictsell.controls.DoubleSellRestrictionControl;
import com.github.mkram17.bazaarutils.features.restrictsell.controls.SellRestrictionControl;
import com.github.mkram17.bazaarutils.features.restrictsell.controls.StringSellRestrictionControl;
import com.teamresourceful.resourcefulconfig.api.annotations.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Category(
        value = "inventory_config",
        categories = {
                InventoryConfig.SellRestrictionsRules.class
        }
)
@ConfigInfo(
        title = "Inventory Config",
        titleTranslation = "bazaarutils.config.inventory.category.value",
        description = "Configurations for the inventory features of the mod",
        descriptionTranslation = "bazaarutils.config.inventory.category.description",
        icon = "box"
)
public final class InventoryConfig {
    @ConfigEntry(
            id = "instantSellRestrictions",
            translation = "bazaarutils.config.inventory.instantSellRestrictions.value"
    )
    @Comment(
            value = "Manages a locking functionality over the Bazaars' §aInstant Sell§r button, with inventory state as well as action restriction.",
            translation = "bazaarutils.config.inventory.instantSellRestrictions.description"
    )
    public static final InstantSellRestrictions instantSellRestrictions = new InstantSellRestrictions(true, 3);

    @Category(value = "instant_sell_rules")
    @ConfigInfo(
            title = "Instant Sell Rules",
            titleTranslation = "bazaarutils.config.inventory.instantSellRestrictions.rules.category.value",
            description = "Manage the rules to be checked by the Instant Sell Restrictions feature",
            descriptionTranslation = "bazaarutils.config.inventory.instantSellRestrictions.rules.category.description",
            icon = "ruler"
    )
    public static final class SellRestrictionsRules {
        @ConfigEntry(id = "numericRestrictionsSeparator")
        @ConfigOption.Hidden
        @ConfigOption.Separator(
                value = "bazaarutils.config.inventory.instantSellRestrictions.rules.separator.numericRestrictions.value",
                description = "bazaarutils.config.inventory.instantSellRestrictions.rules.separator.numericRestrictions.description"
        )
        public static boolean NUMERIC_RESTRICTIONS_SEPARATOR = true;

        @ConfigEntry(
                id = "firstNumericRestriction",
                translation = "bazaarutils.config.inventory.instantSellRestrictions.rules.firstNumericRestriction.value"
        )
        public static final DoubleSellRestrictionControl FIRST_NUMERIC_RESTRICTION = new DoubleSellRestrictionControl(false, NumericRestrictBy.PRICE, 0);

        @ConfigEntry(
                id = "secondNumericRestriction",
                translation = "bazaarutils.config.inventory.instantSellRestrictions.rules.secondNumericRestriction.value"
        )
        public static final DoubleSellRestrictionControl SECOND_NUMERIC_RESTRICTION = new DoubleSellRestrictionControl(false, NumericRestrictBy.PRICE, 0);

        @ConfigEntry(
                id = "thirdNumericRestriction",
                translation = "bazaarutils.config.inventory.instantSellRestrictions.rules.thirdNumericRestriction.value"
        )
        public static final DoubleSellRestrictionControl THIRD_NUMERIC_RESTRICTION = new DoubleSellRestrictionControl(false, NumericRestrictBy.PRICE, 0);

        @ConfigEntry(
                id = "fourthNumericRestriction",
                translation = "bazaarutils.config.inventory.instantSellRestrictions.rules.fourthNumericRestriction.value"
        )
        public static final DoubleSellRestrictionControl FOURTH_NUMERIC_RESTRICTION = new DoubleSellRestrictionControl(false, NumericRestrictBy.PRICE, 0);

        @ConfigEntry(
                id = "fifthNumericRestriction",
                translation = "bazaarutils.config.inventory.instantSellRestrictions.rules.fifthNumericRestriction.value"
        )
        public static final DoubleSellRestrictionControl FIFTH_NUMERIC_RESTRICTION = new DoubleSellRestrictionControl(false, NumericRestrictBy.PRICE, 0);

        @ConfigEntry(id = "stringRestrictionsSeparator")
        @ConfigOption.Hidden
        @ConfigOption.Separator(
                value = "bazaarutils.config.inventory.instantSellRestrictions.rules.separator.stringRestrictions.value",
                description = "bazaarutils.config.inventory.instantSellRestrictions.rules.separator.stringRestrictions.description"
        )
        public static boolean STRING_RESTRICTIONS_SEPARATOR = true;

        @ConfigEntry(
                id = "firstStringRestriction",
                translation = "bazaarutils.config.inventory.instantSellRestrictions.rules.firstStringRestriction.value"
        )
        public static final StringSellRestrictionControl FIRST_STRING_RESTRICTION = new StringSellRestrictionControl(false, "");

        @ConfigEntry(
                id = "secondStringRestriction",
                translation = "bazaarutils.config.inventory.instantSellRestrictions.rules.secondStringRestriction.value"
        )
        public static final StringSellRestrictionControl SECOND_STRING_RESTRICTION = new StringSellRestrictionControl(false, "");

        @ConfigEntry(
                id = "thirdStringRestriction",
                translation = "bazaarutils.config.inventory.instantSellRestrictions.rules.thirdStringRestriction.value"
        )
        public static final StringSellRestrictionControl THIRD_STRING_RESTRICTION = new StringSellRestrictionControl(false, "");

        @ConfigEntry(
                id = "fourthStringRestriction",
                translation = "bazaarutils.config.inventory.instantSellRestrictions.rules.fourthStringRestriction.value"
        )
        public static final StringSellRestrictionControl FOURTH_STRING_RESTRICTION = new StringSellRestrictionControl(false, "");

        @ConfigEntry(
                id = "fifthStringRestriction",
                translation = "bazaarutils.config.inventory.instantSellRestrictions.rules.fifthStringRestriction.value"
        )
        public static final StringSellRestrictionControl FIFTH_STRING_RESTRICTION = new StringSellRestrictionControl(false, "");

        public static final SellRestrictionControl<?>[] ALL = {
                FIRST_NUMERIC_RESTRICTION,
                SECOND_NUMERIC_RESTRICTION,
                THIRD_NUMERIC_RESTRICTION,
                FOURTH_NUMERIC_RESTRICTION,
                FIFTH_NUMERIC_RESTRICTION,
                FIRST_STRING_RESTRICTION,
                SECOND_STRING_RESTRICTION,
                THIRD_STRING_RESTRICTION,
                FOURTH_STRING_RESTRICTION,
                FIFTH_STRING_RESTRICTION
        };

        public static List<SellRestrictionControl<?>> restrictors() {
            return Arrays.stream(ALL).collect(Collectors.toList());
        }
    }
}