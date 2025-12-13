package com.github.mkram17.bazaarutils.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import meteordevelopment.orbit.ICancellable;
import net.hypixel.api.reply.skyblock.SkyBlockBazaarReply;

/**
 * Event fired when bazaar data is updated from the Hypixel API.
 * <p>
 * This event is triggered whenever fresh bazaar market data is retrieved from the Hypixel API.
 * It provides access to the complete bazaar reply containing all current market prices, volumes,
 * and other bazaar statistics.
 * </p>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * {@code
 * @EventHandler
 * public void onBazaarDataUpdate(BazaarDataUpdateEvent event) {
 *     SkyBlockBazaarReply reply = event.getBazaarReply();
 *     // Update local cache with new market data
 *     updatePriceCache(reply);
 * }
 * }
 * </pre>
 * 
 * @see SkyBlockBazaarReply
 */
@AllArgsConstructor
public class BazaarDataUpdateEvent implements ICancellable {

    /**
     * The bazaar data reply from the Hypixel API containing current market information.
     */
    @Getter
    private SkyBlockBazaarReply bazaarReply;

    @Override
    public void setCancelled(boolean cancelled) {
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}