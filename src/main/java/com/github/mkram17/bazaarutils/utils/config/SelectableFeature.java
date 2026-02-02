package com.github.mkram17.bazaarutils.utils.config;

import com.github.mkram17.bazaarutils.config.BUConfigGui;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface SelectableFeature extends ConfigurableFeature {
    static <T extends Enum<T>> Option<T> createOptionHelper(String name, String description, T defaultValue, Class<T> enumClass, Supplier<T> getter, Consumer<T> setter) {
        return Option.<T>createBuilder()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(description)))
                .binding(defaultValue,
                        getter,
                        setter)
                .controller(option -> BUConfigGui.createEnumController(option, enumClass))
                .build();
    }
}
