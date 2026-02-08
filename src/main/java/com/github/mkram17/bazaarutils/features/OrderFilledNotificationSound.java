package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.features.util.BUToggleableFeature;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.text.Text;

@AllArgsConstructor
public class OrderFilledNotificationSound implements BUToggleableFeature {
    @Getter @Setter
    private boolean enabled;
}
