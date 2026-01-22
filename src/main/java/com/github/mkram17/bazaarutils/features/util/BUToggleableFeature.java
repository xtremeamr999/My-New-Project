package com.github.mkram17.bazaarutils.features.util;

import com.github.mkram17.bazaarutils.config.BUConfigGui;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.text.Text;

public interface BUToggleableFeature extends ConfigurableFeature  {
    static Option<Boolean> createOptionHelper(String name, 
                                        String description, 
                                        boolean defaultValue,
                                        java.util.function.Supplier<Boolean> getter,
                                        java.util.function.Consumer<Boolean> setter) 
    {
        return Option.<Boolean>createBuilder()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(description)))
                .binding(defaultValue,
                        getter,
                        setter)
                .controller(BUConfigGui::createBooleanController)
                .build();
    }
}
