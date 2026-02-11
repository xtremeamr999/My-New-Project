package com.github.mkram17.bazaarutils.events.listener;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.config.BUConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for event listeners.
 * <p>
 * This interface defines the contract for classes that need to subscribe to the event bus
 * and handle mod events. Implementing classes should register their event handlers in the
 * {@link #subscribe()} method.
 * </p>
 * 
 * <p><strong>Important:</strong> Implementations must be added to the serialized events list
 * to be persisted with the config, unless there is a singleton object as instance data in
 * {@link BUConfig}, which gets automatically subscribed.</p>
 */
//TODO switch to using fabric event system with annotation processor
public abstract class BUListener implements AbstractListener{

    public static final List<BUListener> listeners = new ArrayList<>();

    public BUListener(){
        listeners.add(this);
    }

    private boolean isSubscribed = false;
    protected boolean runOnInit = true;
    protected boolean subscribeToMeteorEventBus = true;

    /**
     * Subscribes this listener to the event bus.
     * This method should register all event handlers for this listener.
     */
    @Override
    public final void subscribe(){
        if(isSubscribed){
            return;
        }

        isSubscribed = true;
        registerFabricEvents();

        if(subscribeToMeteorEventBus) {
            subscribeToMeteorEventBus();
        }
    }

    protected void registerFabricEvents(){
    }

    private void subscribeToMeteorEventBus(){
        BazaarUtils.EVENT_BUS.subscribe(this);
    }

    public static void subscribeAll(){
        for(BUListener listener : listeners){
            if(listener.runOnInit) {
                listener.subscribe();
            }
        }
    }
}
