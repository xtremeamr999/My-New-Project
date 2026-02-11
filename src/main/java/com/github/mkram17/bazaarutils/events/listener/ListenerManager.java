package com.github.mkram17.bazaarutils.events.listener;

import java.util.ArrayList;
import java.util.List;

public class ListenerManager {

    public static final List<BUListener> listeners = new ArrayList<>();

    public static void subscribeAll(){
        for(BUListener listener : listeners){
            if(listener.runOnInit && !listener.isSubscribed()) {
                listener.subscribe();
            }
        }
    }
}
