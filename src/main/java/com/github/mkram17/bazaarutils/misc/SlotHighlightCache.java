package com.github.mkram17.bazaarutils.misc;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.events.ChestLoadedEvent;
import com.github.mkram17.bazaarutils.features.OrderStatusHighlight;
import com.github.mkram17.bazaarutils.misc.autoregistration.RunOnInit;
import com.github.mkram17.bazaarutils.utils.ScreenInfo;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SlotHighlightCache {

    // key: slotIndex, value: highlightColor
    public static final Map<Integer, Integer> orderStatusHighlightCache = new ConcurrentHashMap<>();
    public static final Map<Integer, Integer> instaSellHighlightCache = new ConcurrentHashMap<>();

    @RunOnInit
    public static void registerScreenEvent(){
        orderStatusHighlightCache.clear();
        instaSellHighlightCache.clear();
    }

    @EventHandler
    public static void updateCaches(ChestLoadedEvent event) {
        var screenInfo = ScreenInfo.getCurrentScreenInfo();
        if(!screenInfo.inMenu(ScreenInfo.BazaarMenuType.ORDER_SCREEN)) return;

        var config = BUConfig.get();
        config.orderStatusHighlight.updateHighlightCache(event.getItemStacks());
    }

    @RunOnInit
    public static void subscribe(){
        BazaarUtils.EVENT_BUS.subscribe(SlotHighlightCache.class);
    }
}
