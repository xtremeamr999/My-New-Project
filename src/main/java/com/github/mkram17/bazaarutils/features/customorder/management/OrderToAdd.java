package com.github.mkram17.bazaarutils.features.customorder.management;

import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Builder
public class OrderToAdd {
    @Getter
    private boolean enabled = true;
    @Getter @Nullable
    private Integer slotNumber;
    @Getter @Nullable
    private Integer orderAmount;
}
