package com.github.mkram17.bazaarutils.events;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.data.BazaarData;
import com.github.mkram17.bazaarutils.events.handlers.BazaarEventHandler;
import com.github.mkram17.bazaarutils.events.handlers.ChatHandler;
import com.github.mkram17.bazaarutils.misc.JoinMessages;
import com.github.mkram17.bazaarutils.utils.*;

import java.util.ArrayList;
import java.util.List;

//TODO switch to using fabric event system with annotation processor
public interface BUListener {
    //must add to getTransientEvents()
    void subscribe();

    static List<BUListener> getEventListeners(){
        return BUConfig.get().getSerializedEvents();
    }
}
