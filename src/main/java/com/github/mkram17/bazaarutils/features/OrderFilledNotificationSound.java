package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.config.BUConfig;
import com.github.mkram17.bazaarutils.config.BUConfigGui;
import com.github.mkram17.bazaarutils.features.util.ToggleableFeature;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.text.Text;

@AllArgsConstructor
public class OrderFilledNotificationSound implements ToggleableFeature {
    @Getter @Setter
    public boolean enabled;
    /**
     * Creates a configuration option for enabling/disabling order filled notification sounds.
     *
     * @return the YACL configuration option for order filled sounds
     */
    private Option<Boolean> createOrderFilledSoundOption() {
        return Option.<Boolean>createBuilder()
                .name(Text.literal("Sound on Order Filled"))
                .description(OptionDescription.of(Text.literal("Plays two short notification sounds when your order is filled.")))
                .binding(true,
                        this::isEnabled,
                        this::setEnabled)
                .controller(BUConfigGui::createBooleanController)
                .build();
    }

    @Override
    public void createOption(ConfigCategory.Builder builder) {
        builder.option(createOrderFilledSoundOption());
    }
}
