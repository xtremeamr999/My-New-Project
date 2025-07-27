package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.events.ChestLoadedEvent;
import com.github.mkram17.bazaarutils.events.handlers.BUListener;
import com.github.mkram17.bazaarutils.misc.orderinfo.OrderPriceInfo;
import com.github.mkram17.bazaarutils.utils.instasell.InstaSellUtil;
import com.github.mkram17.bazaarutils.utils.ScreenInfo;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;

import java.util.*;

import static com.github.mkram17.bazaarutils.BazaarUtils.EVENT_BUS;

public class InstaSellHighlight implements BUListener {

    @Getter @Setter
    private boolean enabled;

    public InstaSellHighlight(boolean enabled) {
        this.enabled = enabled;
    }

    @EventHandler
    private void onScreenLoad(ChestLoadedEvent e) {
        if (!enabled || !ScreenInfo.getCurrentScreenInfo().inMenu(ScreenInfo.BazaarMenuType.BAZAAR_MAIN_PAGE))
            return;
        Map<String, OrderPriceInfo> instaSellOrders = InstaSellUtil.getInstaSellOrders(e.getItemStacks());
    }

    @Override
    public void subscribe() {
        EVENT_BUS.subscribe(this);
    }
}
