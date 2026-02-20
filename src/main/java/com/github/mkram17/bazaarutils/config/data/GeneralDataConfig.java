package com.github.mkram17.bazaarutils.config.data;

import com.github.mkram17.bazaarutils.features.gui.buttons.Bookmarks;
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
            id = "userBazaarTax",
            translation = "bazaarutils.config.general.userBazaarTax.value"
    )
    @Comment(
            value = "The bazaar tax percentage of your current profile considering all possible upgrades.",
            translation = "bazaarutils.config.general.userBazaarTax.description"
    )
    public static double userBazaarTax = 1.125;

    @ConfigButton(
            text = "bazaarutils.config.general.resetBookmarks.runnable",
            title = "bazaarutils.config.general.resetBookmarks.value"
    )
    public static final Runnable resetBookmarks = () -> {
        Bookmarks.bookmarks().clear();
        Bookmarks.saveBookmarks();
    };
}
