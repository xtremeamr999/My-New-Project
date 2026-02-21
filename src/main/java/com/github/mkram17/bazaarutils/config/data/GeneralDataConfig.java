package com.github.mkram17.bazaarutils.config.data;

import com.github.mkram17.bazaarutils.features.gui.buttons.Bookmarks;
import com.github.mkram17.bazaarutils.features.gui.overlays.BazaarLimitsVisualizer;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.Order;
import com.teamresourceful.resourcefulconfig.api.annotations.*;

import java.util.ArrayList;
import java.util.List;

@Category(value = "general_config")
@ConfigInfo(
        title = "General Config",
        titleTranslation = "bazaarutils.config.general.category.value",
        description = "Definitions for general data of the mod",
        descriptionTranslation = "bazaarutils.config.general.category.description",
        icon = "database"
)
public final class GeneralDataConfig {
    public static List<Order> userOrders = new ArrayList<>();

    @ConfigEntry(
            id = "user_bazaar_tax",
            translation = "bazaarutils.config.general.user_bazaar_tax.value"
    )
    @Comment(
            value = "The bazaar tax percentage of your current profile considering all possible upgrades.",
            translation = "bazaarutils.config.general.user_bazaar_tax.description"
    )
    public static double userBazaarTax = 1.125;

    @ConfigButton(
            text = "bazaarutils.config.general.reset_bookmarks.runnable",
            title = "bazaarutils.config.general.reset_bookmarks.value"
    )
    public static final Runnable resetBookmarks = () -> {
        Bookmarks.bookmarks().clear();
        Bookmarks.saveBookmarks();
    };

    @ConfigButton(
            text = "bazaarutils.config.general.reset_limits.runnable",
            title = "bazaarutils.config.general.reset_limits.value"
    )
    public static final Runnable resetLimits = () -> {
        BazaarLimitsVisualizer.limits().clear();
        BazaarLimitsVisualizer.saveLimits();
    };
}
