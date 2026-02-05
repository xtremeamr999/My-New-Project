package com.github.mkram17.bazaarutils.config.data;

import com.github.mkram17.bazaarutils.features.Bookmark;
import com.github.mkram17.bazaarutils.features.customorder.CustomOrder;
import com.github.mkram17.bazaarutils.utils.bazaar.market.order.Order;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

public class GeneralDataConfig {
    @ConfigEntry(id = "userOrders")
    public final List<Order> userOrders = new ArrayList<>();

    @ConfigEntry(id = "customOrders")
    public final List<CustomOrder> customOrders = new ArrayList<>();

    @ConfigEntry(id = "bookmarks")
    public final List<Bookmark> bookmarks = new ArrayList<>();

    @ConfigEntry(id = "userBazaarTax")
    public double userBazaarTax = 1.125;
}
