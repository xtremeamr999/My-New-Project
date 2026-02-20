package com.github.mkram17.bazaarutils.data;

import com.github.mkram17.bazaarutils.features.gui.overlays.OrderLimitVisual;
import com.github.mkram17.bazaarutils.utils.storage.DataStorage;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class OrderLimitStorage {
    private static final Type TYPE = new TypeToken<List<OrderLimitVisual.OrderLimitEntry>>(){}.getType();

    public static final DataStorage<List<OrderLimitVisual.OrderLimitEntry>> INSTANCE = new DataStorage<>(ArrayList::new, "order_limit", TYPE);

    private OrderLimitStorage() {}
}