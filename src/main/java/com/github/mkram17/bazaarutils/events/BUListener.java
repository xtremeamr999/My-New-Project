package com.github.mkram17.bazaarutils.events;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.data.BazaarData;
import com.github.mkram17.bazaarutils.events.handlers.BazaarEventHandler;
import com.github.mkram17.bazaarutils.events.handlers.ChatHandler;
import com.github.mkram17.bazaarutils.misc.JoinMessages;
import com.github.mkram17.bazaarutils.utils.GUIUtils;
import com.github.mkram17.bazaarutils.utils.ItemUpdater;
import com.github.mkram17.bazaarutils.utils.TimeUtil;
import com.github.mkram17.bazaarutils.utils.Util;

import java.util.ArrayList;
import java.util.List;

//TODO switch to using fabric event system with annotation processor
public interface BUListener {
    //must add to getTransientEvents()
    void subscribe();

    //if an instance of the class is not present as an object in BUConfig, it must be added here like the others
    static List<BUListener> getStaticEventListeners(){
        List<BUListener> staticListeners = new ArrayList<>();
        staticListeners.add(ChestLoadedEvent.INSTANCE);
        staticListeners.add(ChatHandler.INSTANCE);
        staticListeners.add(JoinMessages.INSTANCE);
        staticListeners.add(GUIUtils.INSTANCE);
        staticListeners.add(ItemUpdater.INSTANCE);
        staticListeners.add(BazaarData.INSTANCE);
        staticListeners.add(Util.INSTANCE);
        staticListeners.add(TimeUtil.INSTANCE);
        staticListeners.add(BazaarEventHandler.INSTANCE);
        return staticListeners;
    }

    static List<BUListener> getEventListeners(){
        List<BUListener> listeners = getStaticEventListeners();
        listeners.addAll(BUConfig.get().getSerializedEvents());
        return listeners;
    }
}
