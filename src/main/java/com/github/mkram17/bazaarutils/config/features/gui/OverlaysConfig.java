package com.github.mkram17.bazaarutils.config.features.gui;

import com.github.mkram17.bazaarutils.features.gui.overlays.BazaarLimitsVisualizer;
import com.github.mkram17.bazaarutils.features.gui.overlays.PriceCharts;
import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;

@Category(value = "overlays_config")
@ConfigInfo(
        title = "Overlays Config",
        titleTranslation = "bazaarutils.config.overlays.category.value",
        description = "Configurations for the overlays to be created by the mod",
        descriptionTranslation = "bazaarutils.config.overlays.category.description",
        icon = "sidebar"
)
public class OverlaysConfig {
    @ConfigEntry(
            id = "priceCharts",
            translation = "bazaarutils.config.overlays.priceCharts.value"
    )
    @Comment(

            value = "Injects a link to every Bazaar Items' tooltip to quick access relevant market charts.",
            translation = "bazaarutils.config.overlays.priceCharts.description"
    )
    public static final PriceCharts PRICE_CHARTS = new PriceCharts(false, true);

    @ConfigEntry(
            id = "bazaarLimitsVisualizer",
            translation = "bazaarutils.config.overlays.bazaarLimitsVisualizer.value"
    )
    @Comment(
            value = "Injects a link to every Bazaar Items' tooltip to quick access relevant market charts.",
            translation = "bazaarutils.config.overlays.bazaarLimitsVisualizer.description"
    )
    public static final BazaarLimitsVisualizer BAZAAR_LIMITS_VISUALIZER = new BazaarLimitsVisualizer(true);
}

