package com.github.mkram17.bazaarutils.config;

import com.github.mkram17.bazaarutils.BazaarUtils;
import com.github.mkram17.bazaarutils.events.handlers.ChatHandler;
import com.github.mkram17.bazaarutils.features.FlipHelper;
import com.github.mkram17.bazaarutils.features.customorder.CustomOrder;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class BUConfigGui {
    public static Screen create(Screen parent, BUConfig config) {
        return YetAnotherConfigLib.create(BUConfig.HANDLER, (defaults, cfg, builder) -> {
            builder.title(Text.literal(BazaarUtils.MOD_NAME));

            buildGeneralCategory(builder, config);
            buildCustomHelpersCategory(builder);

            if (config.developerMode) {
                buildDeveloperCategory(builder, config.developer);
            }

            return builder;
        }).generateScreen(parent);
    }

    private static void buildGeneralCategory(YetAnotherConfigLib.Builder builder, BUConfig config) {
        ConfigCategory.Builder generalBuilder = ConfigCategory.createBuilder()
                .name(Text.literal("General"));

        config.outbidOrderHandler.createOption(generalBuilder);
        generalBuilder.option(ChatHandler.createOrderFilledSoundOption());
        config.stashMessages.createOption(generalBuilder);
        config.uselessNotificationRemover.createOption(generalBuilder);
        config.priceCharts.createOption(generalBuilder);
        config.orderStatusHighlight.createOption(generalBuilder);
        generalBuilder.option(createDisableErrorNotifsOption(config));
        config.orderLimit.createOption(generalBuilder);
        config.instaSellRestrictions.createOption(generalBuilder);

        builder.category(generalBuilder.build());
    }

    private static void buildCustomHelpersCategory(YetAnotherConfigLib.Builder builder) {
        ConfigCategory.Builder customHelpersBuilder = ConfigCategory.createBuilder()
                .name(Text.literal("Custom Helpers"))
                .tooltip(Text.literal("Add or manage the functionality among the enabled helpers."));

        OptionGroup.Builder CustomOrderGroup = CustomOrder.createOrdersGroup();
        OptionGroup.Builder FlipHelperGroup = FlipHelper.createFlipsGroup();

        CustomOrder.buildOptions(CustomOrderGroup);
        FlipHelper.buildOptions(FlipHelperGroup);

        customHelpersBuilder.group(CustomOrderGroup.build());
        customHelpersBuilder.group(FlipHelperGroup.build());

        builder.category(customHelpersBuilder.build());
    }

    private static void buildDeveloperCategory(YetAnotherConfigLib.Builder builder, BUConfig.Developer developer) {
        ConfigCategory.Builder developerBuilder = ConfigCategory.createBuilder()
                .name(Text.literal("Developer"));

        developerBuilder.option(Option.<Boolean>createBuilder()
                .name(Text.literal("All Messages"))
                .binding(developer.allMessages,
                        () -> developer.allMessages,
                        newVal -> developer.allMessages = newVal)
                .controller(BUConfigGui::createBooleanController)
                .build());

        developerBuilder.group(
                OptionGroup.createBuilder()
                        .name(Text.literal("Message Options"))
                        .description(OptionDescription.of(Text.literal("DEVELOPER ONLY")))
                        .options(developer.createOptions())
                        .build());

        builder.category(developerBuilder.build());
    }

    private static Option<Boolean> createDisableErrorNotifsOption(BUConfig config) {
        return Option.<Boolean>createBuilder()
                .name(Text.literal("Disable Error Notifications"))
                .description(OptionDescription.of(Text.literal("Not recommended to enable this unless you are experiencing error spam. This will disable all error notifications, but not the errors themselves.")))
                .binding(
                        config.disableErrorNotifications,
                        () -> config.disableErrorNotifications,
                        newVal -> config.disableErrorNotifications = newVal
                )
                .controller(BUConfigGui::createBooleanController)
                .build();
    }

    public static BooleanControllerBuilder createBooleanController(Option<Boolean> opt) {
        return BooleanControllerBuilder.create(opt).onOffFormatter().coloured(true);
    }

    public static <T extends Enum<T>> EnumControllerBuilder<T> createEnumController(Option<T> opt, Class<T> enumClass) {
        return EnumControllerBuilder.create(opt).enumClass(enumClass);
    }
}
