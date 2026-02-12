package com.github.mkram17.bazaarutils.events.util;

public enum EventPriorities {
        LOWEST(4),
        LOW(3),
        NORMAL(2),
        HIGH(1),
        HIGHEST(0);

        private final int value;

        EventPriorities(int value) {
            this.value = value;
        }
}
