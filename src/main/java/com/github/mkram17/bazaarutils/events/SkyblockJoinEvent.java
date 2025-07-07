package com.github.mkram17.bazaarutils.events;

import lombok.NoArgsConstructor;
import meteordevelopment.orbit.ICancellable;

//TODO NOT SET UP YET (to do in future)
@NoArgsConstructor
public class SkyblockJoinEvent implements ICancellable{
    @Override
    public void setCancelled(boolean cancelled) {

    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
