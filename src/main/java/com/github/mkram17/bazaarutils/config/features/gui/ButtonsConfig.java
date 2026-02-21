package com.github.mkram17.bazaarutils.config.features.gui;

import com.github.mkram17.bazaarutils.features.gui.buttons.BazaarOpenOrdersButton;
import com.github.mkram17.bazaarutils.features.gui.buttons.BazaarSettingsButton;
import com.github.mkram17.bazaarutils.features.gui.buttons.Bookmarks;
import com.github.mkram17.bazaarutils.features.gui.buttons.CancelOrderAndSearch;
import com.teamresourceful.resourcefulconfig.api.annotations.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@Category(value = "buttons_config")
@ConfigInfo(
        title = "Buttons Config",
        titleTranslation = "bazaarutils.config.buttons.category.value",
        description = "Configurations for the buttons to be injected/handled by the mod",
        descriptionTranslation = "bazaarutils.config.buttons.category.description",
        icon = "pointer"
)
public final class ButtonsConfig {
    @ConfigEntry(id = "general_separator")
    @ConfigOption.Hidden
    @ConfigOption.Separator(
            value = "bazaarutils.config.buttons.separator.general.value",
            description = "bazaarutils.config.buttons.separator.general.description"
    )
    public static boolean GENERAL_BUTTONS_SEPARATOR = true;

    @ConfigEntry(
            id = "mod_settings",
            translation = "bazaarutils.config.buttons.mod_settings.value"
    )
    @Comment(
            value = "Adds a button to selected menus/screen to quick access the mods' settings.",
            translation = "bazaarutils.config.buttons.mod_settings.description"
    )
    public static final BazaarSettingsButton MOD_SETTINGS = new BazaarSettingsButton(true);

    @ConfigEntry(
            id = "open_orders",
            translation = "bazaarutils.config.buttons.open_orders.value"
    )
    @Comment(
            value = """
            Adds a button to selected menus/screen to quick access your orders page.
            
            Requires a §dBooster Cookie§r effect active in order to function.
            """,
            translation = "bazaarutils.config.buttons.open_orders.description"
    )
    public static final BazaarOpenOrdersButton OPEN_ORDERS = new BazaarOpenOrdersButton(true);

    @ConfigEntry(id = "market_separator")
    @ConfigOption.Hidden
    @ConfigOption.Separator(
            value = "bazaarutils.config.buttons.separator.market.value",
            description = "bazaarutils.config.buttons.separator.market.description"
    )
    public static boolean MARKET_BUTTONS_SEPARATOR = true;

    @ConfigEntry(
            id = "cancel_order_and_search",
            translation = "bazaarutils.config.buttons.cancel_order_and_search.value"
    )
    @Comment(
            value = "Adds a button to an unfilled orders' (or offer) settings page to cancel it and search once again the item.",
            translation = "bazaarutils.config.buttons.cancel_order_and_search.description"
    )
    public static final CancelOrderAndSearch CANCEL_ORDER_AND_SEARCH = new CancelOrderAndSearch(false, 25, Items.BLUE_TERRACOTTA.toString());

    @ConfigEntry(
            id = "bookmarks",
            translation = "bazaarutils.config.buttons.bookmarks.value"
    )
    @Comment(
            value = "Adds a button to an item's page to toggle a quick access button to search the same item on the Bazaar.",
            translation = "bazaarutils.config.buttons.bookmarks.description"
    )
    public static final Bookmarks BOOKMARKS = new Bookmarks(true,0, ItemStack.EMPTY, 18, 4);
}