package com.github.mkram17.bazaarutils.misc.orderinfo;

import com.github.mkram17.bazaarutils.config.BUConfig;

import java.util.List;
import java.util.Optional;

public class OrderInfoUtil {
    public static List<BazaarOrder> getUserOrders() {
        return BUConfig.get().userOrders;
    }

    public static Optional<BazaarOrder> getUserOrderFromIndex(int slotIndex) {
        return getUserOrders().stream()
                .filter(order ->
                        order.getItemInfo() != null
                        && order.getItemInfo().slotIndex().equals(slotIndex))
                .findFirst();
    }
}
