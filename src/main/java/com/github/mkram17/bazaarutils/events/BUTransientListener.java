package com.github.mkram17.bazaarutils.events;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.data.BazaarData;
import com.github.mkram17.bazaarutils.misc.JoinMessages;
import com.github.mkram17.bazaarutils.utils.ItemUpdater;
import com.github.mkram17.bazaarutils.utils.Util;

import java.util.ArrayList;
import java.util.List;

public interface BUTransientListener {
    List<BUTransientListener> events = new ArrayList<>();
    //must add to getTransientEvents()
    void subscribe();

    static void addTransientEvents(){
        events.add(new ChestLoadedEvent());
        events.add(new ChatHandler());
        events.add(new JoinMessages());
        events.add(BazaarUtils.gui);
        events.add(new ItemUpdater());
        events.add(new BazaarData());
        events.add(new Util());
    }

    static List<BUTransientListener> getTransientEvents(){
        return events;
    }
}
