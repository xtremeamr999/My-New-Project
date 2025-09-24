package com.github.mkram17.bazaarutils.misc.orderinfo;

import com.github.mkram17.bazaarutils.config.BUConfig;

import java.util.List;
import java.util.Optional;

public class OrderInfoUtil {
    private static List<BazaarOrder> getWatchedOrders() {
        return BUConfig.get().userOrders;
    }

    public static Optional<BazaarOrder> getUserOrderFromIndex(int slotIndex) {
        return getWatchedOrders().stream()
                .filter(order ->
                        order.getItemInfo() != null
                        && order.getItemInfo().slotIndex().equals(slotIndex))
                .findFirst();
    }
}
