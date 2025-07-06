package com.github.mkram17.bazaarutils.events;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.data.BazaarData;
import com.github.mkram17.bazaarutils.misc.JoinMessages;
import com.github.mkram17.bazaarutils.utils.GUIUtils;
import com.github.mkram17.bazaarutils.utils.ItemUpdater;
import com.github.mkram17.bazaarutils.utils.Util;

import java.util.ArrayList;
import java.util.List;

public interface BUListener {
    //must add to getTransientEvents()
    void subscribe();

    //if an instance of the class is not present as an object in BUConfig, it must be added here like the others
    static List<BUListener> getTransientEventListeners(){
        List<BUListener> transientEventListeners = new ArrayList<>();
        transientEventListeners.add(new ChestLoadedEvent());
        transientEventListeners.add(new ChatHandler());
        transientEventListeners.add(new JoinMessages());
        transientEventListeners.add(new GUIUtils());
        transientEventListeners.add(new ItemUpdater());
        transientEventListeners.add(new BazaarData());
        transientEventListeners.add(new Util());
        return transientEventListeners;
    }

    static List<BUListener> getEventListeners(){
        List<BUListener> listeners = getTransientEventListeners();
        listeners.addAll(BUConfig.get().getSerializedEvents());
        return listeners;
    }
}
