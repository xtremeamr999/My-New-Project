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
    @ConfigEntry(id = "generalSeparator")
    @ConfigOption.Hidden
    @ConfigOption.Separator(
            value = "bazaarutils.config.buttons.separator.general.value",
            description = "bazaarutils.config.buttons.separator.general.description"
    )
    public static boolean GENERAL_BUTTONS_SEPARATOR = true;

    @ConfigEntry(id = "modSettings", translation = "bazaarutils.config.buttons.modSettings.value")
    @Comment(
            value = "Adds a button to selected menus/screen to quick access the mods' settings.",
            translation = "bazaarutils.config.buttons.modSettings.description"
    )
    public static final BazaarSettingsButton modSettings = new BazaarSettingsButton(true);

    @ConfigEntry(id = "openOrders", translation = "bazaarutils.config.buttons.openOrders.value")
    @Comment(
            value = """
            Adds a button to selected menus/screen to quick access your orders page.
            
            Requires a §dBooster Cookie§r effect active in order to function.
            """,
            translation = "bazaarutils.config.buttons.openOrders.description"
    )
    public static final BazaarOpenOrdersButton openOrders = new BazaarOpenOrdersButton(true);

    @ConfigEntry(id = "marketSeparator")
    @ConfigOption.Hidden
    @ConfigOption.Separator(
            value = "bazaarutils.config.buttons.separator.market.value",
            description = "bazaarutils.config.buttons.separator.market.description"
    )
    public static boolean MARKET_BUTTONS_SEPARATOR = true;

    @ConfigEntry(id = "cancelOrderAndSearch", translation = "bazaarutils.config.buttons.cancelOrderAndSearch.value")
    @Comment(
            value = "Adds a button to an unfilled orders' (or offer) settings page to cancel it and search once again the item.",
            translation = "bazaarutils.config.buttons.cancelOrderAndSearch.description"
    )
    public static final CancelOrderAndSearch cancelOrderAndSearch = new CancelOrderAndSearch(false, 25, Items.BLUE_TERRACOTTA.toString());

    @ConfigEntry(id = "bookmark", translation = "bazaarutils.config.buttons.bookmark.value")
    @Comment(
            value = "Adds a button to an item's page to toggle a quick access button to search the same item on the Bazaar.",
            translation = "bazaarutils.config.buttons.bookmark.description"
    )
    public static final Bookmarks bookmark = new Bookmarks(true,0, ItemStack.EMPTY, 18, 4);
}