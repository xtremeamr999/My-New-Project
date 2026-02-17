package com.github.mkram17.bazaarutils.config.features;

import com.github.mkram17.bazaarutils.features.*;
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
    public OutbidOrderHandler outbidOrderHandler = new OutbidOrderHandler(false, true, true);

    @ConfigEntry(id = "instaSellRestrictions")
    public InstaSellRestrictions instaSellRestrictions = new InstaSellRestrictions(true, new ArrayList<>());

    @ConfigEntry(id = "orderLimitVisual")
    public OrderLimitVisual orderLimitVisual = new OrderLimitVisual(true);

    @ConfigEntry(id = "showPriceChartsOutsideBazaar")
    public PriceCharts showPriceChartsOutsideBazaar = new PriceCharts(false);

    @ConfigEntry(id = "orderFilledNotificationSound")
    public OrderFilledNotificationSound orderFilledNotificationSound = new OrderFilledNotificationSound(false);

    @ConfigEntry(id = "uselessBazaarNotificationRemover")
    public UselessBazaarNotificationRemover uselessBazaarNotificationRemover = new UselessBazaarNotificationRemover(true);

    @ConfigEntry(id = "stashMessages")
    public StashMessages stashMessages = new StashMessages(false);

    @ConfigEntry(id = "orderStatusHighlight")
    public OrderStatusHighlight orderStatusHighlight = new OrderStatusHighlight(true);

    @ConfigEntry(id = "bazaarOpenOrdersButton")
    public BazaarOpenOrdersButton bazaarOpenOrdersButton = new BazaarOpenOrdersButton(true);

    @ConfigEntry(id = "maxBuyOrder")
    public MaxBuyOrder maxBuyOrder = new MaxBuyOrder(true);

    @ConfigEntry(id = "instaSellHighlight")
    public InstaSellHighlight instaSellHighlight = new InstaSellHighlight(true);
}
