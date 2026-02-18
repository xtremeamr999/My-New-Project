package com.github.mkram17.bazaarutils.features.gui.buttons.inputhelper.customorder;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor
public class OrderToAdd {
    @Getter @Setter
    private boolean enabled = true;
    @Getter @Setter @Nullable
    private Integer slotNumber;
    @Getter @Setter @Nullable
    private Integer orderAmount;
}