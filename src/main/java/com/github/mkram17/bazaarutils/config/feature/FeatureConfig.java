package com.github.mkram17.bazaarutils.config.feature;

import com.github.mkram17.bazaarutils.features.Bookmark;
import com.github.mkram17.bazaarutils.features.FlipHelper;
import com.github.mkram17.bazaarutils.features.customorder.CustomOrder;
import com.github.mkram17.bazaarutils.features.restrictsell.InstaSellRestrictions;
import com.github.mkram17.bazaarutils.utils.bazaar.market.price.PricingPosition;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

public class FeatureConfig {

    @ConfigEntry(id = "customOrders")
    public final List<CustomOrder> customOrders = new ArrayList<>();

    @ConfigEntry(id = "bookmarks")
    public final List<Bookmark> bookmarks = new ArrayList<>();

    @ConfigEntry(id = "flipHelper")
    public FlipHelper flipHelper = new FlipHelper(true, PricingPosition.COMPETITIVE, 17);

    @ConfigEntry(id = "outbidOrderHandler")
    public OutbidOrderHandlerConfig outbidOrderHandler = new OutbidOrderHandlerConfig();

    @ConfigEntry(id = "instaSellRestrictions")
    public InstaSellRestrictions instaSellRestrictions = new InstaSellRestrictions(true, new ArrayList<>());

    @ConfigEntry(id = "enableOrderLimitVisual")
    public boolean enableOrderLimitVisual = true;

    @ConfigEntry(id = "showPriceChartsOutsideBazaar")
    public boolean showPriceChartsOutsideBazaar = false;

    @ConfigEntry(id = "enableOrderFilledNotificationSound")
    public boolean enableOrderFilledNotificationSound = true;

    @ConfigEntry(id = "removeUselessNotifications")
    public boolean removeUselessNotifications = true;

    @ConfigEntry(id = "enableStashMessages")
    public boolean enableStashMessages = false;

    @ConfigEntry(id = "enableOrderStatusHighlight")
    public boolean enableOrderStatusHighlight = true;

    @ConfigEntry(id = "enableBazaarOpenOrdersButton")
    public boolean enableBazaarOpenOrdersButton = true;

    @ConfigEntry(id = "enableMaxBuyOrder")
    public boolean enableMaxBuyOrder = true;

    @ConfigEntry(id = "enableInstaSellHighlight")
    public boolean enableInstaSellHighlight = true;

    public static class OutbidOrderHandlerConfig {
        @ConfigEntry(id = "enableNotifyOutbid")
        public boolean enableNotifyOutbid = false;

        @ConfigEntry(id = "enableNotificationSound")
        public boolean enableNotificationSound = true;

        @ConfigEntry(id = "enableAutoOpen")
        public boolean enableAutoOpen = false;
    }
}
