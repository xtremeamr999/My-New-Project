package com.github.mkram17.bazaarutils.events;

import lombok.Getter;
import meteordevelopment.orbit.ICancellable;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;

/**
 * Event fired when a sign editing screen is opened.
 * <p>
 * This event is triggered when the player opens a sign editing interface, typically when
 * interacting with bazaar order creation or other sign-based input systems. The event can
 * be cancelled to prevent the sign screen from opening.
 * </p>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * {@code
 * @EventHandler
 * public void onSignOpen(SignOpenEvent event) {
 *     if (shouldBlockSignEditing()) {
 *         event.setCancelled(true);
 *         return;
 *     }
 *     // Allow sign to open normally
 * }
 * }
 * </pre>
 * 
 * @see SignEditScreen
 */
public class SignOpenEvent implements ICancellable {
    /**
     * The sign editing screen being opened.
     */
    @Getter
    private final SignEditScreen signEditScreen;
    
    /**
     * Whether this event has been cancelled.
     */
    private boolean cancelled;

    /**
     * Creates a new SignOpenEvent.
     *
     * @param signEditScreen the sign editing screen being opened
     */
    public SignOpenEvent(SignEditScreen signEditScreen) {
        this.signEditScreen = signEditScreen;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}