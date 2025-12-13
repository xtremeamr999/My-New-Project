package com.github.mkram17.bazaarutils.events.handlers;

import com.github.mkram17.bazaarutils.config.BUConfig;

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
public interface BUListener {
    /**
     * Subscribes this listener to the event bus.
     * This method should register all event handlers for this listener.
     */
    void subscribe();

    /**
     * Retrieves the list of all registered event listeners from the config.
     * 
     * @return list of event listeners that are persisted in the configuration
     */
    static List<BUListener> getEventListeners(){
        return BUConfig.get().getSerializedEvents();
    }
}
