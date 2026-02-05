package com.github.mkram17.bazaarutils.config.data;

import com.github.mkram17.bazaarutils.utils.bazaar.market.order.Order;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

public class GeneralDataConfig {
    @ConfigEntry(id = "userOrders")
    public final List<Order> userOrders = new ArrayList<>();

    @ConfigEntry(id = "userBazaarTax")
    public double userBazaarTax = 1.125;
}
