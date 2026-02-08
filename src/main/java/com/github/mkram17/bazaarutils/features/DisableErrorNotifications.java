package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.features.util.BUToggleableFeature;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class DisableErrorNotifications implements BUToggleableFeature {
    @Getter
    @Setter
    private boolean enabled;
}
