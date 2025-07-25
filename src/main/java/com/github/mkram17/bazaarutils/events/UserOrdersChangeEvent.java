package com.github.mkram17.bazaarutils.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import meteordevelopment.orbit.ICancellable;

@AllArgsConstructor
public class UserOrdersChangeEvent implements ICancellable {

    public enum ChangeTypes {
        ADD,
        REMOVE,
        UPDATE
    }
    @Getter
    private ChangeTypes changeType;

    @Override
    public void setCancelled(boolean cancelled) {
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}