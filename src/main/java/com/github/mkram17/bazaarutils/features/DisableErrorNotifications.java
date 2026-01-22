package com.github.mkram17.bazaarutils.features;

import com.github.mkram17.bazaarutils.config.BUConfigGui;
import com.github.mkram17.bazaarutils.features.util.BUToggleableFeature;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.text.Text;

@AllArgsConstructor
public class DisableErrorNotifications implements BUToggleableFeature {
    @Getter
    @Setter
    private boolean enabled;
    private Option<Boolean> createDisableErrorNotifsOption() {
        return Option.<Boolean>createBuilder()
                .name(Text.literal("Disable Error Notifications"))
                .description(OptionDescription.of(Text.literal("Not recommended to enable this unless you are experiencing error spam. This will disable all error notifications, but not the errors themselves.")))
                .binding(enabled,
                        this::isEnabled,
                        this::setEnabled
                )
                .controller(BUConfigGui::createBooleanController)
                .build();
    }

    @Override
    public void createOption(ConfigCategory.Builder builder) {
        builder.option(createDisableErrorNotifsOption());
    }
}
