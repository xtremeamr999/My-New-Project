package com.github.mkram17.bazaarutils.events;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.data.BazaarData;
import com.github.mkram17.bazaarutils.misc.JoinMessages;
import com.github.mkram17.bazaarutils.utils.GUIUtils;
import com.github.mkram17.bazaarutils.utils.ItemUpdater;
import com.github.mkram17.bazaarutils.utils.Util;

import java.util.ArrayList;
import java.util.List;

//TODO switch to using fabric event system with annotation processor
public interface BUListener {
    //must add to getTransientEvents()
    void subscribe();

    //if an instance of the class is not present as an object in BUConfig, it must be added here like the others
    static List<BUListener> getStaticEventListeners(){
        List<BUListener> transientEventListeners = new ArrayList<>();
        transientEventListeners.add(ChestLoadedEvent.INSTANCE);
        transientEventListeners.add(ChatHandler.INSTANCE);
        transientEventListeners.add(JoinMessages.INSTANCE);
        transientEventListeners.add(GUIUtils.INSTANCE);
        transientEventListeners.add(ItemUpdater.INSTANCE);
        transientEventListeners.add(BazaarData.INSTANCE);
        transientEventListeners.add(Util.INSTANCE);
        return transientEventListeners;
    }

    static List<BUListener> getEventListeners(){
        List<BUListener> listeners = getStaticEventListeners();
        listeners.addAll(BUConfig.get().getSerializedEvents());
        return listeners;
    }
}
