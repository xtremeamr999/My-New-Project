package com.github.mkram17.bazaarutils.events.handlers;

import com.github.mkram17.bazaarutils.config.BUConfig;

import java.util.List;

/**
 * Interface for event listeners in the Bazaar Utils mod.
 * <p>
 * This interface defines the contract for classes that need to subscribe to the event bus
 * and handle mod events. Implementing classes should register their event handlers in the
 * {@link #subscribe()} method.
 * </p>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * {@code
 * public class MyEventListener implements BUListener {
 *     @Override
 *     public void subscribe() {
 *         EVENT_BUS.subscribe(this);
 *     }
 *     
 *     @EventHandler
 *     public void onSomeEvent(SomeEvent event) {
 *         // Handle the event
 *     }
 * }
 * }
 * </pre>
 * 
 * <p><strong>Important:</strong> Implementations must be added to the serialized events list
 * to be persisted with the config.</p>
 * 
 * @see BUConfig
 */
//TODO switch to using fabric event system with annotation processor
public interface BUListener {
    /**
     * Subscribes this listener to the event bus.
     * This method should register all event handlers for this listener.
     * 
     * <p><strong>Note:</strong> Must add to getTransientEvents() for proper lifecycle management.</p>
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
